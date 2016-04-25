package bitfinex

import com.google.gson.JsonParser
import com.tars.util.validation.Validator.condition
import common.GenericClient
import global.*
import proto.Order
import proto.Pair
import proto.Trade
import rx.Observable
import util.*
import util.exceptionUtils.wtf
import java.util.concurrent.TimeUnit.MINUTES


/**
 * Generic Bitfinex client
 */
internal class Bitfinex(val config: Config) : GenericClient {
    private val log by logger()
    private val client = net.websocket.client(config.websocketConnectionURL)
    private val tradeChannels = mutableMapOf<Any, TradeChannel>()
    private val bookChannels = mutableMapOf<Any, BookChannel>()

    init {
        client
                .stream()
                .observeOn(cpu.schedulers.io)
                .subscribe(
                        { handleMessage(it) },
                        { wtf(it) }
                )
    }

    override fun start() {
        client.start()
        log.info("started")
    }

    override fun stop() {
        activeTradeStreams().forEach { closeTradeStream(it) }
        client.stop()
    }

    // trade

    override fun streamTrades(pair: Pair): Observable<Trade> {
        log.info("starting trade stream : {}", pair.json());

        return tradeChannels.computeIfAbsent(pair, {
            client.send("{\"event\": \"subscribe\",\"channel\": \"trades\",\"pair\": \"${pair.base.symbol}${pair.quote.symbol}\"}")
            TradeChannel("TRADE|BITFINEX|${pair.base.symbol}|${pair.quote.symbol}", pair)
        }).stream;
    }

    override fun activeTradeStreams(): Set<Pair> {
        return tradeChannels.keys.asSequence()
                .filter { it is Pair }
                .map { it as Pair }
                .toSet()
    }

    override fun closeTradeStream(pair: Pair) {
        client.send("{\"event\":\"unsubscribe\",\"chanId\":\"${tradeChannels.getMandatory(pair).id}\"}");
    }

    // book

    override fun streamBook(pair: Pair): Observable<Order> {
        log.info("starting trade stream : {}", pair.json());

        return bookChannels.computeIfAbsent(pair, {
            client.send("{\"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"${pair.base.symbol}${pair.quote.symbol}\",\"prec\":\"R0\",\"len\":\"full\"}")

            BookChannel("TRADE|BITFINEX|${pair.base.symbol}|${pair.quote.symbol}", pair)
        }).stream;
    }

    override fun activeBookStreams(): Set<Pair> {
        return bookChannels.keys.asSequence()
                .filter { it is Pair }
                .map { it as Pair }
                .toSet()
    }

    override fun closeBookStream(pair: Pair) {
        client.send("{\"event\":\"unsubscribe\",\"chanId\":\"${bookChannels.getMandatory(pair).id}\"}");
    }


    private fun handleMessage(data: String) {
        log.debug("handling websocket message : {}", data)

        val rootElement = JsonParser().parse(data)

        if (rootElement.isJsonObject) {
            val rootObject = rootElement.asJsonObject

            condition(rootObject.has("event"))

            when (rootObject.get("event").asString) {
                "info" -> {
                    condition(rootObject.get("version").asInt == 1, "bitfinex api version mismatch")
                }
                "pong" -> {
                    log.debug("connection is fine with server")
                }
                "subscribed" -> {
                    val chanId = rootObject.get("chanId").asInt
                    val pair = rootObject.get("pair").asString.asPair()

                    when (rootObject.get("channel").asString) {
                        "book" -> {
                            bookChannels.associateKeys(pair, chanId)
                            val channel = bookChannels.getMandatory(chanId)

                            // start heartbeat
                            heartBeat.start(
                                    name = channel.name,
                                    timeout = MINUTES.toMillis(5),
                                    callback = {
                                        mailer.send(
                                                subject = "issue with data stream [${channel.name}]",
                                                message = "timeout reached"
                                        )
                                    })
                        }
                        "trades" -> {
                            tradeChannels.associateKeys(pair, chanId)
                            val channel = tradeChannels.getMandatory(chanId)

                            // start heartbeat
                            heartBeat.start(
                                    name = channel.name,
                                    timeout = MINUTES.toMillis(30),
                                    callback = {
                                        mailer.send(
                                                subject = "issue with data stream [${channel.name}]",
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
                            // find the channel
                            val channel = bookChannels.getMandatory(chanId)

                            // complete the stream
                            channel.stream.onCompleted()
                            tradeChannels.removeWithAssociations(chanId)

                            // stop heartbeat
                            heartBeat.stop(channel.name)
                        }
                        "trades" -> {
                            // find the channel
                            val channel = tradeChannels.getMandatory(chanId)

                            // complete the stream
                            channel.stream.onCompleted()
                            tradeChannels.removeWithAssociations(chanId)

                            // stop heartbeat
                            heartBeat.stop(channel.name)
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

            condition(tradeChannels.containsKey(channelId) || bookChannels.containsKey(channelId))

            if (bookChannels.containsKey(channelId)) {
                val channel = bookChannels.getMandatory(channelId)
                heartBeat.beat(channel.name)

                channel.parseBook(rootArray)
                return
            }

            if (tradeChannels.containsKey(channelId)) {
                val channel = tradeChannels.getMandatory(channelId)
                heartBeat.beat(channel.name)

                channel.parseTrade(rootArray)
                return
            }

            throw IllegalStateException()
        }
    }
}