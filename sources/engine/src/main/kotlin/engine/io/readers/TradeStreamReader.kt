package engine.io.readers

import common.global.trade
import eventstore.tools.io.EventStreamReader
import proto.common.Trade
import rx.Observable
import util.global.notImplemented

/**
 * Analyses raw stream of trades and provides api to query historical data.
 */
class TradeStreamReader(val source: EventStreamReader) {

    fun stream(start: Long = -1, end: Long = -1): Observable<Trade> {
        return Observable.create { subscriber ->
            source.read()
                    .map { trade(it.second) }
                    .filter { (start == -1L || it.time >= start) && (end == -1L || it.time <= end) }
                    .forEach { subscriber.onNext(it) }

            subscriber.onCompleted()
        }
    }
}