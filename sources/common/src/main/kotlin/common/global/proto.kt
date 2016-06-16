package common.global

import com.google.protobuf.ByteString
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import common.Orderbook
import proto.common.Currency
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import util.global.condition
import util.global.dateTime
import util.global.executeAndGetMandatory
import util.global.roundDown3
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern


private object repo {
    val currencies = ConcurrentHashMap<String, Currency>()
    val pairs = ConcurrentHashMap<String, Pair>()
}

// base types ==========================================================================

fun currency(symbol: String): Currency {

    val uSymbol = symbol.toLowerCase()

    return repo.currencies.computeIfAbsent(uSymbol, { Currency.newBuilder().setSymbol(it).build() })
}

fun pair(base: String, quote: String): Pair {

    val uBase = base.toLowerCase()
    val uQuote = quote.toLowerCase()

    return repo.pairs.computeIfAbsent("$uBase|$uQuote", {
        Pair.newBuilder()
                .setBase(currency(uBase))
                .setQuote(currency(quote))
                .build()
    })
}

fun trade(price: Double, volume: Double, time: Long): Trade {
    condition(price > 0 && volume > 0 && time > 0)
    return Trade.newBuilder()
            .setPrice(price)
            .setVolume(volume)
            .setTime(time)
            .build()
}

fun trade(data: ByteArray): Trade {
    return executeAndGetMandatory { Trade.parseFrom(data) }
}

fun order(side: Order.Side, price: Double, volume: Double, time: Long = System.currentTimeMillis()): Order {
    condition(time > 0 && price > 0 && volume >= 0)
    return Order.newBuilder()
            .setTime(time)
            .setSide(side)
            .setPrice(price)
            .setVolume(volume)
            .build();
}

fun order(data: ByteArray): Order {
    return executeAndGetMandatory { Order.parseFrom(data) }
}

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

fun Orderbook.all(): List<Order> {
    return bids + asks
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