package common

import common.internal.isCanceled
import common.internal.place
import common.internal.remove
import proto.common.Order
import java.util.*

/**
 * Aggregated orderbook assumes that all orders fed to it with accept() are aggregated by price,
 * so the matching engine had already aggregated the prices.
 */
class AggregatedOrderbook : IOrderBook {
    internal val asks = TreeMap<Double, Order>(Comparator { o1, o2 -> o1.compareTo(o2) })
    internal val bids = TreeMap<Double, Order>(Comparator { o1, o2 -> o2.compareTo(o1) })

    override fun accept(order: Order) {
        if (order.isCanceled()) {
            this.remove(order)
        } else {
            this.place(order)
        }
    }

    override fun bids(): List<Order> {
        return bids.map { it.value }.toList()
    }

    override fun asks(): List<Order> {
        return asks.map { it.value }.toList()
    }
}


