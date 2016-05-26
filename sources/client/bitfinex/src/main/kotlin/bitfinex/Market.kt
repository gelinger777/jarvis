package bitfinex

import com.google.gson.JsonArray
import common.AggregatedOrderbook
import common.IExchange
import common.IMarket
import common.IOrderBook
import internal.*
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import rx.subjects.PublishSubject
import util.global.logger
import util.global.wtf

internal class Market(val exchange: Bitfinex, val pair: Pair) : IMarket {
    internal val log by logger("bitfinex")

    val trades = PublishSubject.create<Trade>()
    val orders = PublishSubject.create<Order>()

    val book = AggregatedOrderbook()

    val ws = util.net.ws.client(exchange.config.wsURL)
    val channels = mutableMapOf<Any, (JsonArray) -> Unit>()


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
                {
                    log.info("websocket client completed")
                }
        )

        ws.start()
        ws.send(subscribeTradesCommand(pair))
        ws.send(subscribeOrdersCommand(pair))
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
        return orders
    }

    override fun trades(): Observable<Trade> {
        return trades
    }

}