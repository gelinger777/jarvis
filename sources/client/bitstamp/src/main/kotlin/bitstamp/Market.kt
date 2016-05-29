package bitstamp

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

internal class Market(val exchange: Bitstamp, val pair: Pair) : IMarket {
    internal val log by logger("bitstamp")

    val trades = PublishSubject.create<Trade>()
    val orders = PublishSubject.create<Order>()

    val book = AggregatedOrderbook()

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