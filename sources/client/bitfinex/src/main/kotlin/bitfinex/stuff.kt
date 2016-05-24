package bitfinex

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import common.util.asKey
import common.util.asPair
import common.util.trade
import proto.bitfinex.ProtoBitfinex.Order
import proto.common.Pair
import proto.common.Trade
import util.global.associateKeys
import util.global.condition
import util.global.getMandatory
import util.global.wtf

// parsers

internal fun handleMessage(data: String, channels: MutableMap<Any, (JsonArray) -> Unit>) {

    val rootElement = JsonParser().parse(data)

    if (rootElement.isJsonObject) {
        val rootObject = rootElement.asJsonObject

        condition(rootObject.has("event"))

        when (rootObject.get("event").asString) {
            "info" -> {
            }
            "pong" -> {
            }
            "subscribed" -> {
                val chanId = rootObject.get("chanId").asInt
                val pair = rootObject.get("pair").asString.asPair()

                var key = ""

                when (rootObject.get("channel").asString) {
                    "book" -> key = pair.asBookKey()
                    "trades" -> key = pair.asTradeKey()
                    else -> wtf()
                }

                condition(channels.containsKey(key))

                // associate key and channelId with the same channel
                channels.associateKeys(key, chanId)

            }

            else -> {
                wtf("unexpected message from server [$data]")
            }
        }
    } else {

        val rootArray = rootElement.asJsonArray
        val channelId = rootArray.get(0).asInt

        channels.getMandatory(channelId).invoke(rootArray)

    }
}


internal fun parseOrders(array: JsonArray): List<Order> {
    val result = mutableListOf<Order>()

    // either heartbeat or single order
    val element = array.get(1)

    // check if its the first batch
    if (element.isJsonArray) {

        // add all orders in batch
        element.asJsonArray.forEach { jsonOrder ->
            val orderArray = jsonOrder.asJsonArray
            val id = orderArray.get(0).asLong
            val price = orderArray.get(1).asDouble
            val volume = orderArray.get(2).asDouble
            val side = if (volume > 0) Order.Side.BID else Order.Side.ASK

            result.add(Order.newBuilder()
                    .setId(id)
                    .setPrice(price)
                    .setVolume(volume)
                    .setSide(side)
                    .build()
            )
        }

    } else if (element.isJsonPrimitive) {
        val primitive = element.asJsonPrimitive

        if (primitive.isNumber) {
            val id = primitive.asLong;
            val price = array.asJsonArray.get(2).asDouble
            val volume = array.asJsonArray.get(3).asDouble
            val side = if (volume > 0) Order.Side.BID else Order.Side.ASK

            result.add(Order.newBuilder()
                    .setId(id)
                    .setPrice(price)
                    .setVolume(volume)
                    .setSide(side)
                    .build()
            )
        } else {
            // ensure it was heart beat
            condition(primitive.isString && primitive.asString == "hb")
        }
    }
    return result
}

internal fun parseTrades(array: JsonArray): List<Trade> {
    val result = mutableListOf<Trade>()
    val secondElement = array.get(1)
    if (!secondElement.isJsonArray && secondElement.asString == "te") {

        val price = array.get(4).asDouble
        val volume = Math.abs(array.get(5).asDouble)
        val time = array.get(3).asLong * 1000

        // new trade
        result.add(trade(price, volume, time))

    } else {
        // snapshot (we don't need trade snapshot)
    }

    return result
}


// util

internal fun Pair.asTradeKey(): String {
    return "TRADE|${this.asKey()}";
}

internal fun Pair.asBookKey(): String {
    return "BOOK|${this.asKey()}";
}