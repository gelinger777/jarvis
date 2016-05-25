package common.global

import proto.common.Trade
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

object protoRandom {

    private val random = Random()
    private val trades = PublishSubject.create<Trade>()

    val supplier = { trade(random.nextDouble(), random.nextDouble(), System.currentTimeMillis()) }
    var lastPrice = random.nextDouble()

    fun nextTrade(
            price: Double = Math.abs(lastPrice + (random.nextDouble() - 0.5)),
            volume: Double = random.nextDouble(),
            time: Long = System.currentTimeMillis()
    ) {
        trade(price, volume, time)
                .apply { lastPrice = this.price }
                .apply { trades.onNext(this) }
    }

    fun trades(): Observable<Trade> {
        return trades;
    }
}