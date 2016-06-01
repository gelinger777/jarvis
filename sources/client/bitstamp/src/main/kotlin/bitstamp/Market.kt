package bitstamp

import bitstamp.internal.getOrderbookSnapshot
import bitstamp.internal.parseOrdersFromDiff
import common.*
import common.global.all
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

    init {
        // synchronization utility to match snapshots with realtime stream
        val sync = OrderStreamSync(
                fetcher = { getOrderbookSnapshot(pair) }, // snapshot callback
                delay = 3000
        );

        // start realtime stream
        util.net.pusher.stream("de504dc5763aeef9ff52", "diff_order_book", "data")
                .map { parseOrdersFromDiff(it) }
                .subscribe { it.all().forEach { sync.next(it) } }

        // sending synchronized orders to book
        sync.stream.subscribe { book.accept(it) }
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
        return book.stream()
    }

    override fun trades(): Observable<Trade> {
        return trades
    }

}