package common

import common.global.asMap
import proto.common.Order
import rx.Observable
import rx.subjects.PublishSubject
import util.global.getOptional
import util.global.notImplemented
import java.util.*


/**
 * This orderbook is taking full orderbook snapshots, and calculates the diffs.
 */
class DiffAggregatedOrderbook : IOrderBook {

    internal val bids = TreeMap<Double, Order>(Comparator { o1, o2 -> o2.compareTo(o1) })
    internal val asks = TreeMap<Double, Order>(Comparator { o1, o2 -> o1.compareTo(o2) })
    internal val stream = PublishSubject.create<Order>()

    fun accept(snapshot: Orderbook) {
        diff(bids, snapshot.bids.asMap())
        diff(asks, snapshot.asks.asMap())
    }

    private fun diff(book: TreeMap<Double, Order>, newBook: MutableMap<Double, Order>) {
        book.values.forEach { currentOrder ->
            newBook.getOptional(currentOrder.price)
                    .ifPresent { newOrder ->
                        newBook.remove(newOrder.price)

                        // if was edited
                        if (newOrder.volume != currentOrder.volume) {
                            // modify current book
                            book.put(newOrder.price, newOrder)

                            // emit edited order
                            stream.onNext(newOrder)
                        } else {
                            // order was not changed
                        }
                    }
                    .ifNotPresent {
                        // remove from current book
                        book.remove(currentOrder.price)

                        // emit cancellation
                        stream.onNext(currentOrder.toBuilder().setVolume(0.0).build())
                    }
        }

        newBook.values.forEach {
            book.put(it.price, it)
            stream.onNext(it)
        }

    }

    override fun snapshot(): Orderbook {
        throw UnsupportedOperationException()
    }

    fun stream(): Observable<Order> {
        return notImplemented()
    }
}
