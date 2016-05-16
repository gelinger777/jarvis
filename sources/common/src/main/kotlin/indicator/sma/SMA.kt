package indicator.sma

import proto.common.OHLC
import proto.common.SMA
import rx.Observable
import rx.Subscriber
import rx.subjects.PublishSubject
import util.app.log

// extensions functions


fun Observable<OHLC>.indicatorSMA(barCount: Long): Observable<SMA> {
    return this.lift(SimpleMovingAverageOperator(barCount))
}

// implementation specifics

class SimpleMovingAverageOperator(val barCount: Long) : Observable.Operator<SMA, OHLC> {

    override fun call(smaSubscriber: Subscriber<in SMA>): Subscriber<in OHLC> {
        return OHLCSubscriber(smaSubscriber, barCount)
    }
}

class OHLCSubscriber(val observer: Subscriber<in SMA>, val barCount: Long) : Subscriber<OHLC>() {


    val bars = mutableListOf<OHLC>();


    override fun onNext(trade: OHLC) {
        log.info("trade")

    }

    override fun onError(error: Throwable) {
        log.info("error")

    }

    override fun onCompleted() {
        log.info("completed")
    }

}


fun main(args: Array<String>) {
    val tradeStream = PublishSubject.create<OHLC>()

    tradeStream.indicatorSMA(100).subscribe {
        log.info("SMA")
    }

    tradeStream.onNext(OHLC.getDefaultInstance());
    tradeStream.onNext(OHLC.getDefaultInstance());

}