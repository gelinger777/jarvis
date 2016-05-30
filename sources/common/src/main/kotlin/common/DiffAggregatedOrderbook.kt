package common

import common.global.asMap
import proto.common.Order
import rx.Observable
import rx.subjects.PublishSubject
import util.app
import util.global.notContainsKey
import util.global.removeAndGetMandatory
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
        // removals
        book.keys.filter { newBook.notContainsKey(it) }.toList()
                .forEach {
                    val removed = book.removeAndGetMandatory(it)
                    stream.onNext(removed.toBuilder().setVolume(0.0).build())
                }

        // additions and modifications
        newBook.forEach {
            val price = it.key
            val order = it.value

            val existing = book[price]

            if(existing != null && existing.volume == order.volume){
                // order remained unchanged
            }else{
                // order was either added or modified
                book.put(order.price, order)
                stream.onNext(order)
            }
        }

    }

    override fun snapshot(): Orderbook {
        return Orderbook(
                bids = bids.map { it.value }.toList(),
                asks = asks.map { it.value }.toList(),
                time = app.time()
        )
    }

    fun stream(): Observable<Order> {
        return stream
    }
}
