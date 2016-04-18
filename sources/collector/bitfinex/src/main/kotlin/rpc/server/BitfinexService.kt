package rpc.server

import com.google.gson.JsonParser
import com.tars.util.exceptions.ExceptionUtils
import com.tars.util.exceptions.ExceptionUtils.wtf
import com.tars.util.storage.EventStream
import com.tars.util.validation.Validator.condition
import global.*
import io.grpc.stub.StreamObserver
import org.apache.http.client.methods.RequestBuilder.get
import proto.*
import util.*
import util.net.http
import java.util.concurrent.TimeUnit

internal class BitfinexService(val config: Config) : CollectorGrpc.Collector {
    val log by logger()
    val currentVersion = 1

    val websocketClient = net.websocket.client(config.websocketConnectionURL)

    // Channels are mapped both by pair and by bitfinex specific channelId in the same map.
    val realtimeTradeChannels = mutableMapOf<Any, RealtimeTradeChannel>()
    val realtimeBookChannels = mutableMapOf<Any, RealtimeBookChannel>()

    val recordingTradeObservers = mutableMapOf<Pair, StreamObserver<Trade>>()


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

        cleanupTasks.add({
            log.info("shutting down bitfinex websocket client")
            websocketClient.stop()
        })

        websocketClient.start()
    }

    override fun accessibleMarketPairs(request: Empty, observer: StreamObserver<Pairs>) {
        log.debug("getting accessible market pairs")

        http.getString(get("https://api.bitfinex.com/v1/symbols"))
                .map { response ->
                    response.replace(Regex("\\[|\\]|\\n|\""), "")
                            .split(",")
                            .asSequence()
                            .map { str -> str.asPair() }
                            .toList()
                }
                .map { Pairs.newBuilder().addAllPairs(it).build() }
                .ifPresent {
                    observer.onNext(it)
                    observer.onCompleted()
                }
    }

    override fun getCurrentlyRecordingTradePairs(request: Empty, observer: StreamObserver<Pairs>) {
        throw UnsupportedOperationException()
    }

    override fun getCurrentlyRecordingOrderPairs(request: Empty, observer: StreamObserver<Pairs>) {
        throw UnsupportedOperationException()
    }

    override fun shutDown(request: Empty, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun streamTrades(pair: Pair, observer: StreamObserver<Trade>) {
        // get channel or create if necessary
        val channel = realtimeTradeChannels.computeIfAbsent(pair, {
            log.info("starting trade stream : {}", pair.json());

            // send subscription request to bitfinex
            websocketClient.send("{\"event\": \"subscribe\",\"channel\": \"trades\",\"pair\": \"${pair.base.symbol}${pair.quote.symbol}\"}")
            // reserve a channel
            RealtimeTradeChannel(pair)
        })

        // register new observer
        channel.addObserver(observer)
    }

    override fun streamOrders(pair: Pair, observer: StreamObserver<Order>) {
        // get channel or create if necessary
        val channel = realtimeBookChannels.computeIfAbsent(pair, {
            log.info("starting orderbook stream : {}", pair.json());

            // send subscription request to bitfinex
            websocketClient.send("{\"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"${pair.base.symbol}${pair.quote.symbol}\",\"prec\":\"R0\",\"len\":\"full\"}")
            // reserve a channel
            RealtimeBookChannel(pair)
        })
        // register new observer
        channel.addObserver(observer)
    }

    override fun getTradeStreamInfo(pair: Pair, observer: StreamObserver<TradeStreamInfo>) {
        throw UnsupportedOperationException()
    }

    override fun startRecordingTrades(pair: Pair, observer: StreamObserver<ExecutionStatus>) {
        // create recording observable
        val recordingObserver: StreamObserver<Trade> = object : StreamObserver<Trade> {
            val stream = EventStream.get(config.tradeDataPath(pair))

            init {
                // adding this observable to registry
                recordingTradeObservers[pair] = this
            }

            override fun onNext(trade: Trade) {
                // append to persistent event stream
                stream.append(trade.toByteArray());
            }

            override fun onError(error: Throwable) {
                // write the error
                ExceptionUtils.report(error, "Recording was interrupted with error");
                // release chronicle resources
                stream.close()
                // cleaning up the registry
                recordingTradeObservers.remove(pair)
            }

            override fun onCompleted() {
                // release chronicle resources
                stream.close()
                // cleaning up the registry
                recordingTradeObservers.remove(pair)
            }
        }

        // subscribe for trade channel
        streamTrades(pair, recordingObserver)

        // respond with successful execution
        observer.onNext(success())
        observer.onCompleted()
    }

    override fun stopRecordingTrades(pair: Pair, observer: StreamObserver<ExecutionStatus>) {
        // if there is a recording Observer
        recordingTradeObservers.getOptional(pair).ifPresent {
            // unsubscribe it from the channel
            (realtimeTradeChannels[pair] as RealtimeTradeChannel).removeObserver(it)
        }

        observer.onNext(success())
        observer.onCompleted()
    }

    override fun getOrderStreamInfo(pair: Pair, observer: StreamObserver<OrderStreamInfo>) {
        throw UnsupportedOperationException()
    }

    override fun startRecordingOrders(pair: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun stopRecordingOrders(pair: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun exposeHistoricalTradesData(pair: Pair, observer: StreamObserver<InetAddress>) {
        throw UnsupportedOperationException()
    }

    override fun closeHistoricalTradeData(pair: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun exposeHistoricalBookData(pair: Pair, observer: StreamObserver<InetAddress>) {
        throw UnsupportedOperationException()
    }

    override fun closeHistoricalBookData(pair: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
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

                    when (rootObject.get("channel").asString) {
                        "book" -> {
                            // get preserved channel (should be created at request time)
                            val channel = realtimeBookChannels.getMandatory(pair)
                            // associate pair and channelId with the same channel
                            realtimeBookChannels.associateKeys(pair, chanId)
                            // start heartbeat
                            heartBeat.start(
                                    name = channel.name,
                                    timeout = TimeUnit.MINUTES.toMillis(5),
                                    callback = {
                                        mailer.send(
                                                subject = "issue with data stream ${channel.name}",
                                                message = "timeout reached"
                                        )
                                    })
                        }
                        "trades" -> {
                            realtimeTradeChannels.associateKeys(pair, chanId)
                            val channel = realtimeTradeChannels.getMandatory(chanId)

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
                        else -> wtf()
                    }
                }
                "unsubscribed" -> {
                    val chanId = rootObject.get("chanId").asInt

                    when (rootObject.get("channel").asString) {
                        "book" -> {
                            // get the channel
                            val channel = realtimeBookChannels.getMandatory(chanId)
                            // stop heartbeat
                            heartBeat.stop(channel.name)
                            // complete the stream
                            channel.complete()
                            // clean up the map
                            realtimeTradeChannels.removeWithAssociations(chanId)
                        }
                        "trades" -> {
                            // get the channel
                            val channel = realtimeTradeChannels.getMandatory(chanId)
                            // stop heartbeat
                            heartBeat.stop(channel.name)
                            // complete the stream
                            channel.complete()
                            // clean up the map
                            realtimeTradeChannels.removeWithAssociations(chanId)
                        }
                        else -> wtf()
                    }

                    log.debug("unsubscribed from {}", chanId)
                }

                else -> {
                    wtf("unknown message from server [$data]")
                }
            }
        } else {

            val rootArray = rootElement.asJsonArray
            val channelId = rootArray.get(0).asInt

            condition(realtimeTradeChannels.containsKey(channelId) || realtimeBookChannels.containsKey(channelId))

            if (realtimeBookChannels.containsKey(channelId)) {
                val channel = realtimeBookChannels.getMandatory(channelId)
                heartBeat.beat(channel.name)

                channel.parseBook(rootArray)
                return
            }

            if (realtimeTradeChannels.containsKey(channelId)) {
                val channel = realtimeTradeChannels.getMandatory(channelId)
                heartBeat.beat(channel.name)
                channel.parseTrade(rootArray)
                return
            }

            wtf("none of the channel maps have anything for %s", channelId)
        }
    }
}

