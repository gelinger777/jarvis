package bitstamp

import bitstamp.internal.*
import common.*
import common.global.all
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import rx.subjects.PublishSubject
import util.global.filterEmptyOptionals
import util.global.logger
import util.misc.RefCountToggle

internal class Market(val exchange: Bitstamp, val pair: Pair) : IMarket {
    val log by logger("bitstamp")

    val trades = PublishSubject.create<Trade>()
    val book = AggregatedOrderbook()

    val tradesToggle = RefCountToggle(
            on = {
                // stream trades
                log.info("subscribing to trade stream")
                util.net.pusher.stream("de504dc5763aeef9ff52", pair.bitstampTradeStreamKey(), "trade")
                        .map { parseTrade(it) }
                        .filterEmptyOptionals()
                        .subscribe { trades.onNext(it) }
            }
    )


    /**
     * Synchronization mechanism, this will keep querying snapshots of orderbook with 3 second delay,
     * until first snapshot that is younger than the first streamed order, then it will synchronize and
     * stream snapshot orders first and buffered orders after it...
     */
    val sync = OrderStreamSync(
            fetcher = { getOrderbookSnapshot(pair) }, // snapshot callback
            delay = 3000
    ).apply {
        stream.forEach { book.accept(it) }
    }

    val ordersToggle = RefCountToggle(
            on = {
                // stream orders
                log.info("subscribing to order stream")
                util.net.pusher.stream("de504dc5763aeef9ff52", pair.bitstampOrderStreamKey(), "data")
                        .map { parseOrdersFromDiff(it) }
                        .filterEmptyOptionals()
                        .subscribe { it.all().forEach { sync.next(it) } }
            }
    )

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
