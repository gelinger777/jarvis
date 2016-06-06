package bitfinex

import bitfinex.internal.*
import com.google.gson.JsonArray
import common.AggregatedOrderbook
import common.IExchange
import common.IMarket
import common.IOrderBook
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import rx.subjects.PublishSubject
import util.global.logger
import util.global.wtf
import util.misc.RefCountToggle

internal class Market(val exchange: Bitfinex, val pair: Pair) : IMarket {
    internal val log by logger("bitfinex")

    val trades = PublishSubject.create<Trade>()
    val orders = PublishSubject.create<Order>()

    val book = AggregatedOrderbook()

    val ws = util.net.ws.client("wss://api2.bitfinex.com:3000/ws")
    val channels = mutableMapOf<Any, (JsonArray) -> Unit>()

    val tradesToggle = RefCountToggle(on = { ws.send(subscribeTradesCommand(pair)) })
    val ordersToggle = RefCountToggle(on = { ws.send(subscribeOrdersCommand(pair)) })

    init {
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

        ws.stream().subscribe(
                {
                    log.debug("| << {}", it)
                    handleMessage(it, channels)
                },
                {
                    log.error("ws client got unexpected exception", it)
                    wtf(it)
                },
                { log.info("websocket client completed") }
        )

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
        ordersToggle.increment()
        return book.stream()
    }

    override fun trades(): Observable<Trade> {
        tradesToggle.increment()
        return trades
    }

}