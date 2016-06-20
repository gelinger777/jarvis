package common

import common.global.all
import common.internal.diff
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
    val bids = TreeMap<Double, Order>(Comparator { o1, o2 -> o2.compareTo(o1) })
    val asks = TreeMap<Double, Order>(Comparator { o1, o2 -> o1.compareTo(o2) })
    internal val stream = PublishSubject.create<Order>()

    /**
     * Take a snapshot, and generate diff orders.
     */
    fun accept(snapshot: Orderbook){
        bids.diff(snapshot.bids).forEach { accept(it) }
        asks.diff(snapshot.asks).forEach { accept(it) }
    }

    /**
     * Place price aggregated order.
     */
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

    fun clear(){
        bids.clear()
        asks.clear()
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


