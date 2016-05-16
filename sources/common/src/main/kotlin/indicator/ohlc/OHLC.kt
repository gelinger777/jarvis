package indicator.ohlc

import common.util.json
import common.util.trade
import proto.common.OHLC
import proto.common.Trade
import rx.Observable
import rx.Subscriber
import rx.subjects.PublishSubject
import util.app
import util.global.wtf
import java.util.*

fun Observable<Trade>.indicatorOHLC(period: Long, realtime: Boolean = false): Observable<OHLC> {
    return this.lift(OpenHighLowClose(period, realtime))
}

// implementation specifics

class OpenHighLowClose(val period: Long, val realtime: Boolean) : Observable.Operator<OHLC, Trade> {

    override fun call(smaSubscriber: Subscriber<in OHLC>): Subscriber<in Trade> {
        return TradeSubscriber(smaSubscriber, period, realtime)
    }
}

class TradeSubscriber(val targetSubscriber: Subscriber<in OHLC>, var period: Long, val realtime: Boolean) : Subscriber<Trade>() {

    val trades = LinkedList<Trade>()

    var frameStart = -1L;
    var frameEnd = -1L;

    override fun onNext(trade: Trade) {
        initIfNecessary(trade)

        removeObsoleteTrades()

        if (trade.isInFrame()) {
            trades.add(trade)

            if (realtime) {
                emit()
            }

        } else {
            emit()

            trades.clear()
            adjustFrame(trade)
            trades.add(trade)
            if (realtime) {
                emit()
            }
        }
    }

    override fun onError(error: Throwable) {
        wtf()
        app.log.info("error")
    }

    override fun onCompleted() {
        app.log.info("completed")
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

        targetSubscriber.onNext(builder.build())
    }

    private fun Trade.notInFrame(): Boolean {
        return this.time < frameStart || this.time >= frameEnd
    }

    private fun Trade.isInFrame(): Boolean {
        return this.time >= frameStart && this.time < frameEnd
    }

}

fun main(args: Array<String>) {
    val tradeStream = PublishSubject.create<Trade>()

    tradeStream
            .indicatorOHLC(period = 30, realtime = true)

            .subscribe {
                app.log.info(it.json())
            }

    tradeStream.onNext(trade(100.0, 10.0, 10));
    tradeStream.onNext(trade(110.0, 10.0, 20));
    tradeStream.onNext(trade(130.0, 10.0, 30));
    tradeStream.onNext(trade(140.0, 10.0, 40));
    tradeStream.onNext(trade(150.0, 10.0, 50));
    tradeStream.onNext(trade(160.0, 10.0, 60));

}