package engine.readers

import common.global.trade
import eventstore.tools.io.ESReader
import proto.common.Trade
import rx.Observable
import util.global.wtf

/**
 * Analyses raw stream of trades and provides api to query historical data.
 */
class TradeStreamReader(val source: ESReader) {

    fun index(){
        wtf("indexing is not supported yet")
    }

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