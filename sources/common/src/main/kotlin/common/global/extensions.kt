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
fun Observable<Trade>.encode(): Observable<ByteArray> {

    return this.lift(Observable.Operator { subscriber ->

        object : Subscriber<Trade>() {
            var lastTime = -1L

            override fun onNext(trade: Trade) {
                try {
                    // write initial timestamp on first write
                    if (lastTime == -1L) {
                        lastTime = trade.time
                        subscriber.onNext(Longs.toByteArray(lastTime))
                    }

                    // calculate time difference since last time
                    val time = (trade.time - lastTime).toInt()
                    val price = trade.price.toFloat()
                    val vol = trade.volume.toFloat()


                    lastTime = trade.time

                    if (subscriber.isSubscribed()) {
                        subscriber.onNext(Raw.newBuilder().setTime(time).setPrice(price).setVolume(vol).build().toByteArray())
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
fun Observable<ByteArray>.decode(): Observable<Trade> {

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
                        val builder = Trade.newBuilder()

                        val time = lastTime + rawOrder.time

                        builder.time = time

                        val price = rawOrder.price.toDouble()

                        builder.price = price

                        builder.volume = rawOrder.volume.toDouble()

                        val trade = builder.build()

                        lastTime = time

                        if (subscriber.isSubscribed()) {
                            subscriber.onNext(trade)
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