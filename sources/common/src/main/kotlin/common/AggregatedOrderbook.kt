package common

import common.global.all
import common.internal.isCanceled
import common.internal.place
import common.internal.remove
import proto.common.Order
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

/**
 * Aggregated orderbook assumes that all orders fed to it with accept() are aggregated by price,
 * so the matching engine had already aggregated the prices. Aggregated means that a single order in this
 * orderbook might in reality be composed of multiple orders of the same price on the same side...
 */
class AggregatedOrderbook : IOrderBook {
    internal val bids = TreeMap<Double, Order>(Comparator { o1, o2 -> o2.compareTo(o1) })
    internal val asks = TreeMap<Double, Order>(Comparator { o1, o2 -> o1.compareTo(o2) })
    internal val stream = PublishSubject.create<Order>()

    fun accept(order: Order) {

        synchronized(stream, {

            if (order.isCanceled()) {
                this.remove(order)
            } else {
                this.place(order)
            }
            stream.onNext(order)
        })
    }

    override fun snapshot(): Orderbook {
        synchronized(stream, {
            return Orderbook(
                    bids = bids.map { it.value }.toList(),
                    asks = asks.map { it.value }.toList()
            )
        })
    }

    /**
     * Streams current orderbook and all orders after that
     */
    override fun stream(): Observable<Order> {
        return Observable.create { subscriber ->
            synchronized(stream, {
                // stream existing orders
                snapshot().all().forEach { subscriber.onNext(it) }

                // stream from realtime stream
                stream.subscribe(subscriber)
            })
        }
    }
}


