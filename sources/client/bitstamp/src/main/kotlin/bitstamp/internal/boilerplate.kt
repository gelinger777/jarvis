package bitstamp.internal

import com.google.gson.JsonParser
import common.Orderbook
import common.global.order
import common.global.pair
import common.global.trade
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import util.Option
import util.global.executeAndGetSilent
import util.net

fun getOrderbookSnapshot(pair: Pair): Option<Orderbook> {
    return net.http.get("https://www.bitstamp.net/api/v2/order_book/${pair.toBitstampKey()}/")
            .flatMap { parseOrdersFromDiff(it) }
}

fun parseOrdersFromDiff(json: String): Option<Orderbook> {
    return executeAndGetSilent {
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

        Orderbook(bids, asks, timestamp)
    }
}

fun parseTrade(json: String): Option<Trade> {
    return executeAndGetSilent {

        val root = JsonParser().parse(json).asJsonObject

        trade(
                price = root.get("price").asDouble,
                volume =  root.get("amount").asDouble,
                time =  root.get("timestamp").asLong * 1000
        )
    }


}

fun Pair.toBitstampKey(): String {
    return this.base.symbol.toLowerCase() + this.quote.symbol.toLowerCase()
}


fun Pair.bitstampTradeStreamKey(): String {
    if (this.equals(pair("btc", "usd"))) {
        return "live_trades"
    } else {
        return "live_trades_${this.toBitstampKey()}"
    }
}

fun Pair.bitstampOrderStreamKey(): String {
    if (this.equals(pair("btc", "usd"))) {
        return "diff_order_book"
    } else {
        return "diff_order_book_${this.toBitstampKey()}"
    }
}