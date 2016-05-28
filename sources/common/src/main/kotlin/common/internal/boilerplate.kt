package common.internal

import common.AggregatedOrderbook
import proto.common.Order
import util.global.whatever
import util.global.wtf
import java.util.*

internal fun AggregatedOrderbook.remove(order: Order) {
    val book = book(order.side)

    if (!book.containsKey(order.price)) {
        wtf("book should have an order with price : ${order.price}")
    }

    book.remove(order.price)
}

internal fun AggregatedOrderbook.place(order: Order) {
    book(order.side).put(order.price, order)
}

internal fun AggregatedOrderbook.book(side: Order.Side): TreeMap<Double, Order> {
    when (side) {
        Order.Side.ASK -> return asks
        Order.Side.BID -> return bids
        else -> return whatever { wtf("this shouldn't happen") }
    }
}

internal fun Order.isCanceled(): Boolean {
    return this.volume == 0.0
}
