package common

import common.internal.diff
import proto.common.Order
import rx.Observable


/**
 * This orderbook is taking full orderbook snapshots, and calculates the diffs. This orderbook is specifically for idiotic exchange apis like Bitstamp...
 */
class DiffAggregatedOrderbook : IOrderBook {

    internal val book = AggregatedOrderbook()

    fun accept(snapshot: Orderbook) {
        // calculate difference and apply
        book.bids.diff(snapshot.bids).forEach { book.accept(it) }
        book.asks.diff(snapshot.asks).forEach { book.accept(it) }
    }

    override fun snapshot(): Orderbook {
        return book.snapshot()
    }

    override fun stream(): Observable<Order> {
        return book.stream
    }
}
