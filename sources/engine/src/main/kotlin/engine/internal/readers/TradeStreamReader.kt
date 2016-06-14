package engine.internal.readers

import common.global.trade
import eventstore.tools.StreamReader
import proto.common.Trade
import rx.Observable
import util.global.condition
import util.global.wtf

/**
 * Analyses raw stream of trades and provides api to query historical data.
 */
class TradeStreamReader(val source: StreamReader) {

    fun index(){
        wtf("indexing is not supported yet")
    }

    fun stream(start: Long, end: Long): Observable<Trade> {
        condition(0 < start && start < end, "illegal arguments")

        return Observable.create { subscriber ->
            source.read()
                    .map { trade(it.second) }
                    .filter { it.time >= start && it.time <= end }
                    .forEach { subscriber.onNext(it) }

            subscriber.onCompleted()
        }
    }
}