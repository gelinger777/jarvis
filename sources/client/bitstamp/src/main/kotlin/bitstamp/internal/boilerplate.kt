package bitstamp.internal

import com.google.gson.JsonParser
import common.Orderbook
import common.global.order
import proto.common.Order
import proto.common.Pair
import util.Option
import util.net

fun getOrderbookSnapshot(pair: Pair): Option<Orderbook> {
    return net.http.get("https://www.bitstamp.net/api/v2/order_book/${pair.toBitstampKey()}/")
            .map { parseOrdersFromDiff(it) }
}


fun parseOrdersFromDiff(json: String): Orderbook {
    val root = JsonParser().parse(json).asJsonObject

    // bitstamp uses unix time (which is seconds) we use milliseconds
    val timestamp = root.get("timestamp").asLong * 1000

    val bids = root.get("bids").asJsonArray.map {
        order(
                side = Order.Side.BID,
                price = it.asJsonArray.get(0).asDouble,
                volume = it.asJsonArray.get(1).asDouble,
                time = timestamp
        )
    }.toList()

    val asks = root.get("asks").asJsonArray.map {
        order(
                side = Order.Side.ASK,
                price = it.asJsonArray.get(0).asDouble,
                volume = it.asJsonArray.get(1).asDouble,
                time = timestamp
        )
    }.toList()

    return Orderbook(bids, asks, timestamp)
}

fun Pair.toBitstampKey(): String {
    return this.base.symbol.toLowerCase() + this.quote.symbol.toLowerCase()
}