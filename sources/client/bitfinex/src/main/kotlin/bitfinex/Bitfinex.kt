package bitfinex

import bitfinex.channel.BookChannel
import bitfinex.channel.BroadCoastingChannel
import bitfinex.channel.TradeChannel
import com.google.gson.JsonParser
import common.util.asKey
import common.util.asPair
import common.util.json
import org.apache.http.client.methods.RequestBuilder.get
import proto.bitfinex.BitfinexConfig
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import util.*
import util.global.*
import util.net.http
import java.util.concurrent.TimeUnit

class Bitfinex(val config: BitfinexConfig) {
    val log by logger("bitfinex")
    val version = 1

    private val websocketClient = net.websocket.client(config.websocketConnectionURL)

    // Channels are mapped both by pair and by bitfinex specific channelId in the same map.
    private val channels = mutableMapOf<Any, BroadCoastingChannel<out Any>>()

    init {
        log.info("starting")
        websocketClient
                .stream()
                .observeOn(cpu.schedulers.io)
                .subscribe(
                        {
                            log.debug("| << {}", it)
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

    //    fun stop() {
    //        log.info("stopping")
    //        channels.keys.filter { it is Int }.forEach {
    //            websocketClient.send("{\"event\":\"unsubscribe\",\"chanId\":\"$it\"}");
    //        }
    //    }

    fun symbols(): List<Pair> {
        log.info("getting accessible market pairs")

        return http.getString(get("https://api.bitfinex.com/v1/symbols"))
                .map { response ->
                    response.replace(Regex("\\[|\\]|\\n|\""), "")
                            .split(",")
                            .asSequence()
                            .map { str -> str.asPair() }
                            .toList()
                }
                .ifNotPresentCompute { emptyList<Pair>() }
                .get()
    }


    fun streamTrades(pair: Pair): Observable<Trade> {
        // get channel or create if necessary
        val channel = channels.computeIfAbsent(pair.asTradeKey(), {
            log.info("starting trade stream : {}", pair.json());

            // send subscription request to bitfinex
            websocketClient.send("{\"event\": \"subscribe\",\"channel\": \"trades\",\"pair\": \"${pair.base.symbol}${pair.quote.symbol}\"}")
            // reserve a channel
            TradeChannel(pair.asTradeKey())
        })

        return (channel as TradeChannel).observable

    }

    fun streamOrders(pair: Pair): Observable<Order> {

        // get channel or create if necessary
        val channel = channels.computeIfAbsent(pair.asBookKey(), {
            log.info("starting orderbook stream : {}", pair.json());

            // send subscription request to bitfinex
            websocketClient.send("{\"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"${pair.base.symbol}${pair.quote.symbol}\",\"prec\":\"R0\",\"len\":\"full\"}")
            // reserve a channel
            BookChannel(pair.asBookKey())
        })

        // register new observer
        return (channel as BookChannel).observable
    }

    // stuff

    private fun handleMessage(data: String) {

        val rootElement = JsonParser().parse(data)

        if (rootElement.isJsonObject) {
            val rootObject = rootElement.asJsonObject

            condition(rootObject.has("event"))

            when (rootObject.get("event").asString) {
                "info" -> {
                    condition(rootObject.get("version").asInt == version, "bitfinex api version mismatch")
                }
                "pong" -> {
                    log.info("connection is fine with server")
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

                    log.info("unsubscribed from {}", chanId)
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

    fun Pair.asTradeKey(): String {
        return "TRADE|${this.asKey()}";
    }

    fun Pair.asBookKey(): String {
        return "BOOK|${this.asKey()}";
    }
}

