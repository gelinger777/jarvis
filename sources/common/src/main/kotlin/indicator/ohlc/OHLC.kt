package indicator.ohlc

import common.util.json
import proto.common.OHLC
import proto.common.Trade
import rx.Observable
import rx.Subscriber
import util.app
import util.global.wtf
import java.util.*

fun Observable<Trade>.OHLC(period: Long): Observable<OHLC> {
    return this.lift {
        object : Subscriber<Trade>() {

            val trades = LinkedList<Trade>()

            var frameStart = -1L;
            var frameEnd = -1L;

            override fun onNext(trade: Trade) {
                initIfNecessary(trade)

                removeObsoleteTrades()

                if (trade.isInFrame()) {
                    trades.add(trade)
                } else {
                    emit()
                    trades.clear()
                    adjustFrame(trade)
                    trades.add(trade)
                }
            }

            override fun onError(error: Throwable) {
                wtf()
            }

            override fun onCompleted() {
            }

            // stuff

            private fun initIfNecessary(trade: Trade) {
                if (frameStart == -1L && frameEnd == -1L) {
                    frameStart = trade.time
                    frameEnd = trade.time + period
                }
            }

            private fun removeObsoleteTrades() {
                val iterator = trades.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (next.time < frameStart) {
                        app.log.info("removed : ${next.json()}")
                        iterator.remove()
                    } else {
                        break
                    }
                }
            }


            private fun adjustFrame(trade: Trade) {
                while (trade.notInFrame()) {
                    frameStart = frameEnd
                    frameEnd += period
                }
            }

            private fun emit() {
                val builder = OHLC.newBuilder()

                // compute open and close
                builder.open = trades.first.price
                builder.close = trades.last.price

                builder.high = -1.0
                builder.low = -1.0

                builder.start = frameStart
                builder.end = frameEnd

                trades.forEach {
                    val price = it.price
                    val volume = it.volume

                    // compute volume
                    builder.vol += volume

                    // compute high
                    if (builder.high == -1.0 || builder.high < price) {
                        builder.high = price
                    }

                    // compute low
                    if (builder.low == -1.0 || builder.low > price) {
                        builder.low = price
                    }
                }

                it.onNext(builder.build())
            }

            private fun Trade.notInFrame(): Boolean {
                return this.time < frameStart || this.time >= frameEnd
            }

            private fun Trade.isInFrame(): Boolean {
                return this.time >= frameStart && this.time < frameEnd
            }

        }
    }
}
