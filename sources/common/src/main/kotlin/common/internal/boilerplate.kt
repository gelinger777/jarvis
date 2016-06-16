package common.internal

import common.AggregatedOrderbook
import proto.common.Order
import util.global.report
import util.global.wtf
import java.util.*

internal fun Order.isCanceled(): Boolean {
    return this.volume == 0.0
}

internal fun AggregatedOrderbook.remove(order: Order) {

    // try remove from bids
    var removed = bids.remove(order.price)

    if (removed == null) {
        removed = asks.remove(order.price)
    }

    if(removed == null){
        report("book should have an order with price : ${order.price} (this either means we are not synced with orderbook or api is written by idiots (like Bitstamp)...")
    }
}

internal fun AggregatedOrderbook.place(order: Order) {
    book(order.side).put(order.price, order)
}

internal fun AggregatedOrderbook.book(side: Order.Side): TreeMap<Double, Order> {
    when (side) {
        Order.Side.ASK -> return asks
        Order.Side.BID -> return bids
        else -> wtf()
    }
}