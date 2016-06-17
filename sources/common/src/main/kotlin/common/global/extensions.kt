package common.global

import com.google.protobuf.ByteString
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import common.Orderbook
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import util.global.condition
import util.global.dateTime
import util.global.roundDown3
import java.util.regex.Pattern

fun String.asPair(): Pair {
    val matcher = Pattern.compile("^(.{3})[-|\\||_/|-]?(.{3})$").matcher(this)
    // BTCUSD
    // BTC_USD
    // BTC|USD
    // BTC/USD
    // BTC-USD

    condition(matcher.find(), "$this is not a valid pair string")

    return pair(
            base = matcher.group(1),
            quote = matcher.group(2)
    )
}

fun List<Order>.asMap(): MutableMap<Double, Order> {
    val map = mutableMapOf<Double, Order>()
    forEach { map.put(it.price, it) }
    return map
}

fun Pair.compact(): String {
    return "${base.symbol}|${quote.symbol}"
}

fun Trade.compact(showTime: Boolean = true): String {
    if (showTime) {
        return "${time.dateTime()} - ${price.roundDown3()} | ${volume.roundDown3()}"
    } else {
        return "${price.roundDown3()} | ${volume.roundDown3()}"
    }
}

fun Order.compact(showTime: Boolean = true): String {
    if (showTime) {
        return "${time.dateTime()} - $side | ${price.roundDown3()} | ${volume.roundDown3()}"
    } else {
        return "$side | ${price.roundDown3()} | ${volume.roundDown3()}"
    }
}

/**
 * Convert to json
 */
fun MessageOrBuilder.json(pretty: Boolean = false): String {
    val json = JsonFormat.printer().print(this)

    if (!pretty) {
        return json.replace(Regex("[ |\\n]+"), " ")
    }
    return json
}

fun ByteArray.toByteString(): ByteString {
    return ByteString.copyFrom(this)
}
fun Orderbook.all(): List<Order> {
    return bids + asks
}