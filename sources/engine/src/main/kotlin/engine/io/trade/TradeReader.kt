package engine.io.trade

import com.google.common.primitives.Longs
import eventstore.tools.io.BytesReader
import proto.common.Raw
import proto.common.Trade
import rx.Observable
import util.global.condition

/**
 * Analyses raw stream of trades and provides api to query historical data.
 */
class TradeReader(val source: BytesReader) {

    fun stream(start: Long = -1, end: Long = -1): Observable<Trade> {
        return Observable.create { subscriber ->

            var lastTime = -1L

            source.read().forEach {
                if (lastTime == -1L) {
                    condition(it.second.size == 8)
                    lastTime = Longs.fromByteArray(it.second)
                }else{
                    val rawOrder = Raw.parseFrom(it.second)
                    val builder = Trade.newBuilder()

                    val time = lastTime + rawOrder.time

                    builder.time = time

                    val price = rawOrder.price.toDouble()

                    builder.price = price

                    builder.volume = rawOrder.volume.toDouble()

                    val trade = builder.build()

                    lastTime = time

                    if((start == -1L || trade.time >= start) && (end == -1L || trade.time <= end)){
                        subscriber.onNext(trade)
                    }
                }
            }

            subscriber.onCompleted()
        }
    }
}