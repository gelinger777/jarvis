package common

import common.internal.isCanceled
import common.internal.place
import common.internal.remove
import proto.common.Order
import java.util.*

/**
 * Aggregated orderbook assumes that all orders fed to it with accept() are aggregated by price,
 * so the matching engine had already aggregated the prices. Aggregated means that a single order in this
 * orderbook might in reality be composed of multiple orders of the same price on the same side...
 */
class AggregatedOrderbook : IOrderBook {

    internal val bids = TreeMap<Double, Order>(Comparator { o1, o2 -> o2.compareTo(o1) })
    internal val asks = TreeMap<Double, Order>(Comparator { o1, o2 -> o1.compareTo(o2) })

    fun accept(order: Order) {
        if (order.isCanceled()) {
            this.remove(order)
        } else {
            this.place(order)
        }
    }

    override fun snapshot(): Orderbook {
        return Orderbook(
                bids = bids.map { it.value }.toList(),
                asks = asks.map { it.value }.toList()
        )
    }

}


