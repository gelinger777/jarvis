package collector.bitfinex.server

import collector.bitfinex.server.channel.BookChannel
import collector.bitfinex.server.channel.BroadCoastingChannel
import collector.bitfinex.server.channel.TradeChannel
import collector.bitfinex.server.recorder.RecordingObserver
import com.google.gson.JsonParser
import com.tars.util.validation.Validator.condition
import eventstore.storage
import global.*
import io.grpc.stub.StreamObserver
import org.apache.http.client.methods.RequestBuilder.get
import proto.common.*
import util.*
import util.exceptionUtils.wtf
import util.net.http
import java.util.concurrent.TimeUnit

internal class BitfinexService(val config: Config) : CollectorGrpc.Collector {
    val log by logger()
    val currentVersion = 1

    val websocketClient = net.websocket.client(config.websocketConnectionURL)

    // Channels are mapped both by pair and by bitfinex specific channelId in the same map.
    val channels = mutableMapOf<Any, BroadCoastingChannel<out Any>>()
    val recorders = mutableMapOf<String, Any>()

    init {
        log.info("starting")
        websocketClient
                .stream()
                .observeOn(cpu.schedulers.io)
                .subscribe(
                        {
                            log.debug("| << {}")
                            handleMessage(it)
                        },
                        {
                            log.error("ws client got unexpected exception", it)
                            wtf(it)
                        },
                        {
                            log.info("websocket client completed")
                        }
                )

        cleanupTasks.add("bitfinex-websocket-client", { websocketClient.stop() })

        websocketClient.start()
    }

    override fun status(request: CollStatusReq, observer: StreamObserver<CollStatusResp>) {
        log.debug("getting accessible market pairs")


        val supportedPairs = http.getString(get("https://api.bitfinex.com/v1/symbols"))
                .map { response ->
                    response.replace(Regex("\\[|\\]|\\n|\""), "")
                            .split(",")
                            .asSequence()
                            .map { str -> str.asPair() }
                            .toList()
                }
                .ifNotPresentCompute { emptyList<Pair>() }
                .get()

        respondCollStatus(observer, supportedPairs)
    }

    override fun shutdown(request: CollShutdownReq, observer: StreamObserver<CollShutdownResp>) {
//        channels.values.forEach { it.complete() }
//        channels.clear()
//
//        websocketClient.stop()

        respondCollShutdown(observer, success = false)
    }

    override fun streamTrades(request: StreamTradesReq, observer: StreamObserver<Trade>) {
        val pair = request.pair
        // get channel or create if necessary
        val channel = channels.computeIfAbsent(pair.asTradeKey(), {
            log.info("starting trade stream : {}", pair.json());

            // send subscription request to bitfinex
            websocketClient.send("{\"event\": \"subscribe\",\"channel\": \"trades\",\"pair\": \"${pair.base.symbol}${pair.quote.symbol}\"}")
            // reserve a channel
            TradeChannel(pair.asTradeKey())
        })

        // register new observer
        (channel as TradeChannel).addObserver(observer)
    }

    override fun streamOrders(request: StreamOrdersReq, observer: StreamObserver<Order>) {
        val pair = request.pair

        // get channel or create if necessary
        val channel = channels.computeIfAbsent(pair.asBookKey(), {
            log.info("starting orderbook stream : {}", pair.json());

            // send subscription request to bitfinex
            websocketClient.send("{\"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"${pair.base.symbol}${pair.quote.symbol}\",\"prec\":\"R0\",\"len\":\"full\"}")
            // reserve a channel
            BookChannel(pair.asBookKey())
        })

        // register new observer
        (channel as BookChannel).addObserver(observer)
    }

    override fun recordTrades(request: RecordTradesReq, observer: StreamObserver<RecordTradesResp>) {
        val pair = request.pair

        recorders.computeIfAbsent(config.tradeDataPath(pair), {
            // create recording channel
            val recorder = RecordingObserver<Trade>(it)
            // stream trades to recorder
            streamTrades(requestStreamTrades(pair), recorder)
            recorder
        })

        respondRecordTrades(observer, success = true)

    }

    override fun recordOrders(request: RecordOrdersReq, observer: StreamObserver<RecordOrdersResp>) {
        val pair = request.pair

        recorders.computeIfAbsent(config.bookDataPath(pair), {
            // create recording channel
            val recorder = RecordingObserver<Order>(it)

            streamOrders(requestStreamOrders(pair), recorder)
            recorder
        })

        respondRecordOrders(observer, success = true)
    }


    override fun streamHistoricalTrades(request: StreamHistoricalTradesReq, observer: StreamObserver<Trade>) {
        val path = config.tradeDataPath(request.pair)
        val stream = storage.eventStream(path)

        stream.stream(request.startIndex, request.endIndex).subscribe { observer.onNext(Trade.parseFrom(it)) }
        observer.onCompleted()
    }

    override fun streamHistoricalOrders(request: StreamHistoricalOrdersReq, observer: StreamObserver<Order>) {
        val path = config.bookDataPath(request.pair)
        val stream = storage.eventStream(path)

        stream.stream(request.startIndex, request.endIndex).subscribe { observer.onNext(Order.parseFrom(it)) }
        observer.onCompleted()
    }

    // stuff

    private fun handleMessage(data: String) {
        log.debug("handling websocket message : {}", data)

        val rootElement = JsonParser().parse(data)

        if (rootElement.isJsonObject) {
            val rootObject = rootElement.asJsonObject

            condition(rootObject.has("event"))

            when (rootObject.get("event").asString) {
                "info" -> {
                    condition(rootObject.get("version").asInt == currentVersion, "bitfinex api version mismatch")
                }
                "pong" -> {
                    log.debug("connection is fine with server")
                }
                "subscribed" -> {
                    val chanId = rootObject.get("chanId").asInt
                    val pair = rootObject.get("pair").asString.asPair()

                    var key = ""

                    when (rootObject.get("channel").asString) {
                        "book" -> key = pair.asBookKey()
                        "trades" -> key = pair.asTradeKey()
                        else -> wtf()
                    }

                    val channel = channels.getMandatory(key)
                    // associate key and channelId with the same channel
                    channels.associateKeys(key, chanId)
                    // start heartbeat
                    heartBeat.start(
                            name = channel.name,
                            timeout = TimeUnit.MINUTES.toMillis(30),
                            callback = {
                                mailer.send(
                                        subject = "issue with data stream ${channel.name}",
                                        message = "timeout reached"
                                )
                            })
                }
                "unsubscribed" -> {
                    val chanId = rootObject.get("chanId").asInt

                    // get the channel
                    val channel = channels.getMandatory(chanId)
                    // stop heartbeat
                    heartBeat.stop(channel.name)
                    // complete the stream
                    channel.complete()
                    // clean up the map
                    channels.removeWithAssociations(chanId)

                    log.debug("unsubscribed from {}", chanId)
                }

                else -> {
                    wtf("unknown message from server [$data]")
                }
            }
        } else {

            val rootArray = rootElement.asJsonArray
            val channelId = rootArray.get(0).asInt

            channels.getMandatory(channelId)
                    .parse(rootArray)

        }
    }
}

