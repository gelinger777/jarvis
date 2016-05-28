package bitstamp.internal

import com.google.gson.JsonParser
import common.OrderBatch
import common.OrderStreamSync
import common.global.order
import common.global.pair
import proto.common.Order
import proto.common.Pair
import util.Option
import util.net

fun getOrderbookSnapshot(pair: Pair): Option<OrderBatch> {
    return net.http.get("https://www.bitstamp.net/api/v2/order_book/${pair.toBitstampKey()}/")
            .map { parseOrdersFromDiff(it) }

}


fun parseOrdersFromDiff(json: String): OrderBatch {
    val orders = mutableListOf<Order>()
    val root = JsonParser().parse(json).asJsonObject

    val timestamp = root.get("timestamp").asLong

    root.get("bids").asJsonArray.forEach {
        orders.add(order(
                side = Order.Side.BID,
                price = it.asJsonArray.get(0).asDouble,
                volume = it.asJsonArray.get(1).asDouble,
                time = timestamp * 1000
        ))
    }

    root.get("asks").asJsonArray.forEach {
        orders.add(order(
                side = Order.Side.ASK,
                price = it.asJsonArray.get(0).asDouble,
                volume = it.asJsonArray.get(1).asDouble,
                time = timestamp * 1000
        ))
    }

    return OrderBatch(timestamp, orders)
}


fun main(args: Array<String>) {

    val pair = pair("btc", "usd")

    val sync = OrderStreamSync({ getOrderbookSnapshot(pair) }, 1000);
//
//    net.pusher.stream("de504dc5763aeef9ff52", "diff_order_book", "data")
//            .map { parseOrdersFromDiff(it) }
//            .subscribe { it.orders.forEach { sync.feedOrder(it) } }
//
//
//
//
//    readLine()
}


fun Pair.toBitstampKey(): String {
    return this.base.symbol.toLowerCase() + this.quote.symbol.toLowerCase()
}