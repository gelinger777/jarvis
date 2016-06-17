package eventstore.tools.io.order

import com.google.common.primitives.Longs
import common.AggregatedOrderbook
import common.global.all
import eventstore.tools.io.bytes.BytesReader
import proto.common.Order
import proto.common.Raw
import rx.Observable
import util.global.condition
import util.global.logger

/**
 * Analyses raw stream of orders and provides api to query historical data. Streaming orders does not make sense if the initial orderbook is not being streamed first, hence it will create different orderbook snapshots.
 */
class OrderReader(val source: BytesReader) {
    val log = logger("OrderStreamReader")

    var lastTime = -1L

    fun stream(start: Long = -1, end: Long = -1): Observable<Order> {
        return Observable.create { subscriber ->
            // before streaming actual order events we need to stream snapshot of orderbook at that exact moment
            val book = AggregatedOrderbook()

            source.read()
                    .forEach {
                        if (lastTime == -1L) {
                            condition(it.second.size == 8)
                            lastTime = Longs.fromByteArray(it.second)
                        } else {
                            val rawOrder = Raw.parseFrom(it.second)

                            val builder = Order.newBuilder()

                            val time = lastTime + rawOrder.time

                            builder.time = time

                            val price = rawOrder.price.toDouble()

                            builder.price = price

                            if (rawOrder.volume < 0) {
                                builder.side = Order.Side.ASK
                                builder.volume = -rawOrder.volume.toDouble()
                            } else {
                                builder.side = Order.Side.BID
                                builder.volume = rawOrder.volume.toDouble()
                            }

                            val order = builder.build()

                            lastTime = time

                            //  already streaming (good for branch prediction)
                            if (start == -1L || order.time > start) {
                                // emit and proceed
                                subscriber.onNext(order)
                            } else {
                                // maintain valid orderbook until we hit start
                                book.accept(order)

                                // if start is found
                                if (order.time >= start) {
                                    // emit orderbook first
                                    book.snapshot().all().forEach { subscriber.onNext(it) }
                                    // free up used memory
                                    book.clear()
                                }
                            }
                        }
                    }

            subscriber.onCompleted()
        }
    }
}