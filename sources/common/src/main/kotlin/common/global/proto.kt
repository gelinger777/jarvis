package common.global

import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import io.grpc.stub.StreamObserver
import proto.bitfinex.ProtoBitfinex.BitfinexConfig
import proto.common.*
import util.global.condition
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

    val matcher = Pattern.compile("(.{3})[-|\\||/|-]?(.{3})").matcher(this)
    // BTCUSD
    // BTC|USD
    // BTC/USD
    // BTC-USD

    matcher.find()

    return pair(
            base = matcher.group(1),
            quote = matcher.group(2)
    )
}

fun Pair.asFolderName(): String {
    // btc-usd
    return "${this.base.symbol.toLowerCase()}-${this.quote.symbol.toLowerCase()}"
}

fun Pair.asKey(): String {
    return "${this.base.symbol}|${this.quote.symbol}"
}

/**
 * Convert to single line json representation
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
    return this.start == other.start && this.end == other.end
}