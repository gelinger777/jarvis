package common.internal

import common.AggregatedOrderbook
import proto.common.Order
import util.global.report
import util.global.wth
import java.util.*

internal fun Order.isCanceled(): Boolean {
    return this.volume == 0.0
}

internal fun AggregatedOrderbook.remove(order: Order) {
    val book = book(order.side)

    if (!book.containsKey(order.price)) {
        report("book should have an order with price : ${order.price} (this either means we are not synced with orderbook or api is written by idiots (like Bitstamp)...")
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
        else -> return wth()
    }
}