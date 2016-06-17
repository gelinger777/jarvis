package common.global

import com.google.common.primitives.Longs
import com.google.protobuf.ByteString
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import common.Orderbook
import proto.common.Order
import proto.common.Pair
import proto.common.Raw
import proto.common.Trade
import rx.Observable
import rx.Subscriber
import util.global.condition
import util.global.dateTime
import util.global.isSubscribed
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

/**
 * Encode trade stream to compressed byte stream.
 */
fun Observable<Trade>.encodeTrades(): Observable<ByteArray> {

    return this.lift(Observable.Operator { subscriber ->

        object : Subscriber<Trade>() {
            var lastTime = -1L

            override fun onNext(order: Trade) {
                try {
                    // write initial timestamp on first write
                    if (lastTime == -1L) {
                        lastTime = order.time
                        subscriber.onNext(Longs.toByteArray(lastTime))
                    }

                    // calculate time difference since last time
                    val builder = Raw.newBuilder()

                    builder.time = (order.time - lastTime).toInt()
                    builder.price = order.price.toFloat()
                    builder.volume = order.volume.toFloat()

                    lastTime = order.time

                    if (subscriber.isSubscribed()) {
                        subscriber.onNext(builder.build().toByteArray())
                    }

                } catch(e: Throwable) {
                    subscriber.onError(e)
                }
            }

            override fun onError(error: Throwable) {
                subscriber.onError(error)
            }

            override fun onCompleted() {
                subscriber.onCompleted()
            }
        }
    })
}

/**
 * Decode compressed byte stream to trade stream.
 */
fun Observable<ByteArray>.decodeTrades(): Observable<Trade> {

    return this.lift(Observable.Operator { subscriber ->
        object : Subscriber<ByteArray>() {
            var lastTime = -1L

            override fun onNext(data: ByteArray) {
                try {
                    if (lastTime == -1L) {
                        condition(data.size == 8, "first chunk shall be 8 bytes (initial timestamp)")
                        lastTime = Longs.fromByteArray(data)
                    } else {
                        val rawTrade = Raw.parseFrom(data)
                        val builder = Trade.newBuilder()

                        builder.time = lastTime + rawTrade.time
                        builder.price = rawTrade.price.toDouble()
                        builder.volume = rawTrade.volume.toDouble()

                        lastTime = builder.time

                        if (subscriber.isSubscribed()) {
                            subscriber.onNext(builder.build())
                        }
                    }
                } catch(e: Throwable) {
                    subscriber.onError(e)
                }
            }

            override fun onError(error: Throwable) {
                subscriber.onError(error)
            }

            override fun onCompleted() {
                subscriber.onCompleted()
            }
        }
    })
}


/**
 * Encode trade stream to compressed byte stream.
 */
fun Observable<Order>.encodeOrders(): Observable<ByteArray> {

    return this.lift(Observable.Operator { subscriber ->

        object : Subscriber<Order>() {
            var lastTime = -1L

            override fun onNext(order: Order) {
                try {
                    // write initial timestamp on first write
                    if (lastTime == -1L) {
                        lastTime = order.time
                        subscriber.onNext(Longs.toByteArray(lastTime))
                    }

                    // calculate time difference since last time
                    val builder = Raw.newBuilder()

                    builder.time = (order.time - lastTime).toInt()
                    builder.price = order.price.toFloat()
                    builder.volume = if (order.side == Order.Side.BID) order.volume.toFloat() else -order.volume.toFloat()

                    lastTime = order.time

                    if (subscriber.isSubscribed()) {
                        subscriber.onNext(builder.build().toByteArray())
                    }

                } catch(e: Throwable) {
                    subscriber.onError(e)
                }
            }

            override fun onError(error: Throwable) {
                subscriber.onError(error)
            }

            override fun onCompleted() {
                subscriber.onCompleted()
            }
        }
    })
}

/**
 * Decode compressed byte stream to trade stream.
 */
fun Observable<ByteArray>.decodeOrders(): Observable<Order> {

    return this.lift(Observable.Operator { subscriber ->
        object : Subscriber<ByteArray>() {
            var lastTime = -1L

            override fun onNext(data: ByteArray) {
                try {
                    if (lastTime == -1L) {
                        condition(data.size == 8, "first chunk shall be 8 bytes (initial timestamp)")
                        lastTime = Longs.fromByteArray(data)
                    } else {
                        val rawOrder = Raw.parseFrom(data)
                        val builder = Order.newBuilder()

                        builder.time = lastTime + rawOrder.time
                        builder.price = rawOrder.price.toDouble()

                        if (rawOrder.volume > 0) {
                            builder.side = Order.Side.BID
                            builder.volume = rawOrder.volume.toDouble()
                        } else {
                            builder.side = Order.Side.ASK
                            builder.volume = -rawOrder.volume.toDouble()
                        }

                        lastTime = builder.time

                        if (subscriber.isSubscribed()) {
                            subscriber.onNext(builder.build())
                        }
                    }
                } catch(e: Throwable) {
                    subscriber.onError(e)
                }
            }

            override fun onError(error: Throwable) {
                subscriber.onError(error)
            }

            override fun onCompleted() {
                subscriber.onCompleted()
            }
        }
    })
}