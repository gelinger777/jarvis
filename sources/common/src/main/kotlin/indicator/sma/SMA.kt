package indicator.ohlc

import common.global.json
import common.global.protoRandom
import proto.common.OHLC
import proto.common.SMA
import rx.Observable
import rx.Subscriber
import util.app
import util.global.wtf
import java.util.*

fun Observable<OHLC>.SMA(count: Int): Observable<SMA> {
    return this.lift {

        object : Subscriber<OHLC>() {

            val bars = LinkedList<OHLC>()

            override fun onNext(bar: OHLC) {
                // add current bar to the stack
                bars.addFirst(bar)

                // ensure correct size of the stack
                if (bars.size > count) {
                    bars.removeLast()
                }

                // calculate

                if (bars.size == count) {
                    val simpleMovingAverage = bars.map { it.close }.sum() / count

                    val builder = SMA.newBuilder()
                    builder.start = bars.last.start
                    builder.end = bars.first.end
                    builder.price = simpleMovingAverage

                    it.onNext(builder.build())
                }
            }

            override fun onError(error: Throwable) {
                wtf()
            }

            override fun onCompleted() {
            }
        }
    }
}

fun main(args: Array<String>) {

    protoRandom.trades()
            .doOnNext { app.log.info("TRADE : ${it.json()}") }
            .OHLC(3)
            .doOnNext { app.log.info("OHLC : ${it.json()}") }
            .SMA(5)
            .subscribe { app.log.info("SMA : ${it.json()}") }

    var price = 1.0
    var time = 1L
    for(i in 1..20) {
        protoRandom.nextTrade(price++, 1.0, time++)
    }

}
