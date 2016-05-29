package btce.internal

import com.google.gson.JsonParser
import common.Orderbook
import common.global.asPair
import common.global.order
import proto.common.Order.Side.ASK
import proto.common.Order.Side.BID
import proto.common.Pair
import util.Option
import util.app
import util.global.executeAndGetSilent
import util.net.http


internal fun pollPairs(): Option<List<Pair>> {
    return http.get("https://btc-e.com/api/3/info")
            .flatMap { parsePairs(it) }
}

internal fun parsePairs(json: String): Option<List<Pair>> {
    return executeAndGetSilent {
        JsonParser().parse(json).asJsonObject.getAsJsonObject("pairs")
                .entrySet().map { it.key.asPair() }
                .toList()
    }
}

internal fun pollOrders(pair: Pair): Option<Orderbook> {
    val key = pair.toBtceKey()

    return http.get("https://btc-e.com/api/3/depth/$key")
            .flatMap { parseOrderbook(it, key) }
}

internal fun parseOrderbook(json: String, key: String): Option<Orderbook> {
    return executeAndGetSilent {
        val timestamp = app.time()
        val book = JsonParser().parse(json).asJsonObject.get(key).asJsonObject

        val bids = book.get("bids").asJsonArray
                .map { it.asJsonArray }
                .map {
                    order(
                            side = BID,
                            price = it.get(0).asDouble,
                            volume = it.get(1).asDouble,
                            time = timestamp
                    )
                }.toList()

        val asks = book.get("asks").asJsonArray
                .map { it.asJsonArray }
                .map {
                    order(
                            side = ASK,
                            price = it.get(0).asDouble,
                            volume = it.get(1).asDouble,
                            time = timestamp
                    )
                }.toList()

        Orderbook(bids, asks, timestamp)
    }
}

fun Pair.toBtceKey(): String {
    return "${base.symbol.toLowerCase()}_${quote.symbol.toLowerCase()}"
}
