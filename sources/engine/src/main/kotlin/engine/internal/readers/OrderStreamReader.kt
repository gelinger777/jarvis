package engine.internal.readers

import common.AggregatedOrderbook
import common.global.all
import common.global.compact
import common.global.order
import eventstore.tools.StreamReader
import proto.common.Order
import rx.Observable
import util.global.condition
import util.global.logger
import util.global.wtf

/**
 * Analyses raw stream of orders and provides api to query historical data. Streaming orders does not make sense if the initial orderbook is not being streamed first, hence it will create different orderbook snapshots.
 *
 * todo : using eventstore-tools
 */
class OrderStreamReader(val source: StreamReader) {
    val log = logger("orderStreamReader")

    fun index() {
        wtf("indexing is not supported yet")
    }

    fun stream(start: Long, end: Long): Observable<Order> {
        condition(0 < start && start < end, "illegal arguments")

        return Observable.create { subscriber ->
            // before streaming actual order events we need to stream snapshot of orderbook at that exact moment
            val book = AggregatedOrderbook()

            source.read()
                    .doOnNext { log.trace { "${it.first} : ${source.path}" } }
                    .map { order(it.second) }
                    .doOnNext { log.trace { "${it.compact()}" } }
                    .filter { it.time <= end }
                    .forEach {
                        // already streaming (good for branch prediction)
                        if(it.time > start){
                            // emit and proceed
                            subscriber.onNext(it)
                        } else {
                            // maintain valid orderbook until we hit start
                            book.accept(it)

                            // if start is found
                            if (it.time == start) {
                                // emit orderbook first
                                book.snapshot().all().forEach { subscriber.onNext(it) }
                                // free up used memory
                                book.clear()
                            }
                        }
                    }

            subscriber.onCompleted()
        }
    }
}