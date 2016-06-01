package common.global

import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import common.Orderbook
import io.grpc.stub.StreamObserver
import proto.bitfinex.ProtoBitfinex.BitfinexConfig
import proto.common.*
import util.global.condition
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern


private object repo {
    val currencies = ConcurrentHashMap<String, Currency>()
    val pairs = ConcurrentHashMap<String, Pair>()
}

// base types ==========================================================================

fun currency(symbol: String): Currency {

    val uSymbol = symbol.toUpperCase()

    return repo.currencies.computeIfAbsent(uSymbol, { Currency.newBuilder().setSymbol(it).build() })
}

fun pair(base: String, quote: String): Pair {

    val uBase = base.toUpperCase()
    val uQuote = quote.toUpperCase()

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

fun order(side: Order.Side, price: Double, volume: Double, time: Long = System.currentTimeMillis()): Order {
    condition(time > 0 && price > 0 && volume >= 0)
    return Order.newBuilder()
            .setTime(time)
            .setSide(side)
            .setPrice(price)
            .setVolume(volume)
            .build();
}

fun String.asPair(): Pair {
    val matcher = Pattern.compile("(.{3})[-|\\||_/|-]?(.{3})").matcher(this)
    // BTCUSD
    // BTC_USD
    // BTC|USD
    // BTC/USD
    // BTC-USD

    matcher.find()

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

fun Pair.asFolderName(): String {
    // btc-usd
    return "${base.symbol.toLowerCase()}-${quote.symbol.toLowerCase()}"
}

fun Pair.compact(): String {
    return "${base.symbol}|${quote.symbol}"
}

fun Trade.compact(showTime: Boolean = false):String{
    if(showTime) {
        return "${time.dateTime()} - ${price.roundDown3()}|${volume.roundDown3()}"
    }else{
        return "${price.roundDown3()}|${volume.roundDown3()}"
    }
}

fun Order.compact(showTime: Boolean = false): String {

    if (showTime) {
        return "${time.dateTime()} - $side|${price.roundDown3()}|${volume.roundDown3()}"
    } else {
        return "$side|${price.roundDown3()}|${volume.roundDown3()}"
    }
}

fun Double.roundDown3(): Double {
    return Math.floor(this * 1e3) / 1e3
}

fun Double.roundDown5(): Double {
    return Math.floor(this * 1e5) / 1e5
}

fun Double.roundDown7(): Double {
    return Math.floor(this * 1e7) / 1e7
}

fun Long.dateTime(): String {
    return DateTimeFormatter.ofPattern("YYY-MM-dd HH:mm:ss").format(ZonedDateTime.ofInstant (Instant.ofEpochMilli(this), ZoneOffset.UTC))
}

//fun main(args: Array<String>) {
//    order(Order.Side.BID, 405.0, 10.2).compact(true).apply { println(this) }
//}

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

// services ============================================================================

fun address(host: String, port: Int): ServiceAddress {
    return ServiceAddress.newBuilder()
            .setHost(host)
            .setPort(port)
            .build()
}

fun bitfinexConfig(wsUrl: String, publicKey: String, privateKey: String): BitfinexConfig {
    return BitfinexConfig.newBuilder()
            .setWsURL(wsUrl)
            .setPublicKey(publicKey)
            .setPrivateKey(privateKey)
            .build()
}

// request

fun requestStreamTrades(pair: Pair): StreamTradesReq {
    return StreamTradesReq.newBuilder().setPair(pair).build()
}

fun requestStreamOrders(pair: Pair): StreamOrdersReq {
    return StreamOrdersReq.newBuilder().setPair(pair).build()
}

// response

fun respondCollInfo(observer: StreamObserver<CollInfoResp>, accessiblePairs: List<Pair>) {
    observer.onNext(CollInfoResp.newBuilder().addAllAccessibleMarketPairs(accessiblePairs).build());
    observer.onCompleted()
}

fun respondRecordTrades(observer: StreamObserver<RecordTradesResp>, success: Boolean) {
    observer.onNext(RecordTradesResp.newBuilder().setSuccess(success).build());
    observer.onCompleted()
}

fun respondRecordOrders(observer: StreamObserver<RecordOrdersResp>, success: Boolean) {
    observer.onNext(RecordOrdersResp.newBuilder().setSuccess(success).build());
    observer.onCompleted()
}

fun OHLC.haveSameTimeRange(other: OHLC): Boolean {
    return start == other.start && end == other.end
}