package indicator.ohlc

import proto.common.OHLC
import proto.common.Trade
import rx.Observable
import rx.Subscriber
import rx.subjects.PublishSubject
import util.app
import java.util.concurrent.TimeUnit

fun Observable<Trade>.indicatorOHLC(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Observable<OHLC> {
    return this.lift(OpenHighLowClose(time, unit))
}

// implementation specifics

class OpenHighLowClose(val time: Long, val unit: TimeUnit) : Observable.Operator<OHLC, Trade> {

    override fun call(smaSubscriber: Subscriber<in OHLC>): Subscriber<in Trade> {
        return TradeSubscriber(smaSubscriber, unit.toMillis(time))
    }
}

class TradeSubscriber(
        val targetSubscriber: Subscriber<in OHLC>,
        val time: Long) : Subscriber<Trade>() {


    val trades = mutableListOf<Trade>();


    override fun onNext(trade: Trade) {
        app.log.info("trade")

        val endTime = trade.time
        val startTime = endTime - time

        trades.removeAll { it.time < startTime || it.time > endTime }
        trades.add(trade)







        targetSubscriber.onNext(OHLC.getDefaultInstance())
    }

    override fun onError(error: Throwable) {
        app.log.info("error")

    }

    override fun onCompleted() {
        app.log.info("completed")
    }

}


fun main(args: Array<String>) {
    val tradeStream = PublishSubject.create<Trade>()

    tradeStream.indicatorOHLC(100).subscribe {
        app.log.info("OHLC")
    }

    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());

}