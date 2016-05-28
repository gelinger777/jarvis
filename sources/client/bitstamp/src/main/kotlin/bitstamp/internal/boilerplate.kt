package bitstamp.internal

import com.google.gson.JsonParser
import common.OrderBatch
import common.global.order
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

    // bitstamp uses unix time (which is seconds) we use milliseconds
    val timestamp = root.get("timestamp").asLong * 1000

    root.get("bids").asJsonArray.forEach {
        orders.add(order(
                side = Order.Side.BID,
                price = it.asJsonArray.get(0).asDouble,
                volume = it.asJsonArray.get(1).asDouble,
                time = timestamp
        ))
    }

    root.get("asks").asJsonArray.forEach {
        orders.add(order(
                side = Order.Side.ASK,
                price = it.asJsonArray.get(0).asDouble,
                volume = it.asJsonArray.get(1).asDouble,
                time = timestamp
        ))
    }

    return OrderBatch(timestamp, orders)
}

fun Pair.toBitstampKey(): String {
    return this.base.symbol.toLowerCase() + this.quote.symbol.toLowerCase()
}