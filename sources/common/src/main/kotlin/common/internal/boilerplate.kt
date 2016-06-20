package common.internal

import common.AggregatedOrderbook
import common.global.asMap
import proto.common.Order
import util.global.getMandatory
import util.global.notContainsKey
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

fun TreeMap<Double, Order>.diff(orders: List<Order>): List<Order> {
    val diff = mutableListOf<Order>()
    val target = orders.asMap()

    // removals
    this.keys.filter { target.notContainsKey(it) }.toList()
            .forEach { diff.add(this.getMandatory(it).toBuilder().setVolume(0.0).build()) }

    // additions and modifications
    target.values.forEach {
        val existing = this[it.price]

        if (existing == null || existing.volume != it.volume) {
            diff.add(it)
        }
    }

    return diff
}