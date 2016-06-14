package bitfinex

import bitfinex.internal.*
import com.google.gson.JsonArray
import common.AggregatedOrderbook
import common.IExchange
import common.IMarket
import common.IOrderBook
import common.global.compact
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import rx.subjects.PublishSubject
import util.global.logger
import util.misc.RefCountToggle

internal class Market(val exchange: Bitfinex, val pair: Pair) : IMarket {
    internal val log = logger("${exchange.name}|${pair.compact()}")

    val trades = PublishSubject.create<Trade>()
    val orders = PublishSubject.create<Order>()

    val book = AggregatedOrderbook()

    val ws = util.net.ws.client("wss://api2.bitfinex.com:3000/ws")
    val channels = mutableMapOf<Any, (JsonArray) -> Unit>()

    val tradesToggle = RefCountToggle(on = { ws.send(subscribeTradesCommand(pair)) })
    val ordersToggle = RefCountToggle(on = { ws.send(subscribeOrdersCommand(pair)) })

    init {
        log.info { "initializing" }

        orders.subscribe { book.accept(it) }

        channels.put(pair.asTradeKey(), object : (JsonArray) -> Unit {
            override fun invoke(data: JsonArray) {
                parseTrades(data).forEach { trades.onNext(it) }
            }
        })

        channels.put(pair.asBookKey(), object : (JsonArray) -> Unit {
            override fun invoke(data: JsonArray) {
                parseOrders(data).forEach { orders.onNext(it) }
            }
        })

        ws.stream()
                .doOnNext { log.debug { "websocket event : $it" } }
                .forEach { handleMessage(it, channels) }

        ws.start()
    }

    override fun exchange(): IExchange {
        return exchange
    }

    override fun pair(): Pair {
        return pair
    }

    override fun orderbook(): IOrderBook {
        return book
    }

    override fun orders(): Observable<Order> {
        log.info { "streaming orders" }
        ordersToggle.increment()
        return book.stream()
    }

    override fun trades(): Observable<Trade> {
        log.info { "streaming trades" }
        tradesToggle.increment()
        return trades
    }

}