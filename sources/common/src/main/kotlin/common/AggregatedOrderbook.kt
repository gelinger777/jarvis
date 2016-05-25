package common

import proto.common.Order
import util.global.whatever
import util.global.wtf
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


fun AggregatedOrderbook.remove(order: Order) {
    val book = book(order.side)

    if (!book.containsKey(order.price)) {
        wtf("book should have an order with price : ${order.price}")
    }

    book.remove(order.price)
}

fun AggregatedOrderbook.place(order: Order) {
    book(order.side).put(order.price, order)
}

fun AggregatedOrderbook.book(side: Order.Side): TreeMap<Double, Order> {
    when (side) {
        Order.Side.ASK -> return asks
        Order.Side.BID -> return bids
        else -> return whatever { wtf("this shouldn't happen") }
    }
}

fun Order.isCanceled(): Boolean {
    return this.volume == 0.0
}
