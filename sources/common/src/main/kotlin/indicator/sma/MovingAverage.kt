package indicator.sma

import common.IHavePrice
import common.IHaveTimestamp
import proto.common.SMA
import proto.common.Trade
import rx.Observable
import rx.Subscriber
import rx.subjects.PublishSubject
import util.app.log
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

// extensions functions


fun Observable<Trade>.indicatorSMA(time: Long, unit: TimeUnit = MILLISECONDS, streamInRealtime: Boolean = false): Observable<SMA> {
    return this.lift(SimpleMovingAverageOperator(time, unit, streamInRealtime))
}

//fun projectSMA(tradeStream: Observable<Trade>, time: Long, unit: TimeUnit = MILLISECONDS, streamInRealtime: Boolean = false): Observable<SMA> {
//    return tradeStream.lift(SimpleMovingAverageOperator(time, unit, streamInRealtime))
//}

// implementation specifics

class SimpleMovingAverageOperator(val time: Long, val unit: TimeUnit, val streamInRealtime: Boolean) : Observable.Operator<SMA, Trade> {

    override fun call(smaSubscriber: Subscriber<in SMA>): Subscriber<in Trade> {
        return TradeSubscriber(smaSubscriber, time, unit, streamInRealtime)
    }
}

class Somthing : IHavePrice, IHaveTimestamp{
    override fun price(): Double {
        throw UnsupportedOperationException()
    }

    override fun time(): Long {
        throw UnsupportedOperationException()
    }

}

class TradeSubscriber(
        val targetSubscriber: Subscriber<in SMA>,
        val time: Long,
        val unit: TimeUnit,
        val streamInRealtime: Boolean) : Subscriber<Trade>() {


    val trades = mutableListOf<Trade>();


    override fun onNext(trade: Trade) {
        log.info("trade")




        targetSubscriber.onNext(SMA.getDefaultInstance())
    }

    override fun onError(error: Throwable) {
        log.info("error")

    }

    override fun onCompleted() {
        log.info("completed")
    }

}


fun main(args: Array<String>) {
    val tradeStream = PublishSubject.create<Trade>()

    tradeStream.indicatorSMA(100).subscribe {
        log.info("SMA")
    }

    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());
    tradeStream.onNext(Trade.getDefaultInstance());

}