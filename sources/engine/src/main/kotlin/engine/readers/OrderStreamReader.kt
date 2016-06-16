package engine.readers

import common.AggregatedOrderbook
import common.global.all
import common.global.compact
import common.global.order
import eventstore.tools.io.EventStreamReader
import proto.common.Order
import rx.Observable
import util.global.logger
import util.global.notImplemented

/**
 * Analyses raw stream of orders and provides api to query historical data. Streaming orders does not make sense if the initial orderbook is not being streamed first, hence it will create different orderbook snapshots.
 */
class OrderStreamReader(val source: EventStreamReader) {
    val log = logger("orderStreamReader")

    fun index() {
        notImplemented()
    }

    fun stream(start: Long = -1, end: Long = -1): Observable<Order> {
        return Observable.create { subscriber ->
            // before streaming actual order events we need to stream snapshot of orderbook at that exact moment
            val book = AggregatedOrderbook()

            source.read()
                    .doOnNext { log.trace { "${it.first} : ${source.path}" } }
                    .map { order(it.second) }
                    .doOnNext { log.trace { "${it.compact()}" } }
                    .filter { end == -1L || it.time <= end }
                    .forEach {
                        // already streaming (good for branch prediction)
                        if(start == -1L || it.time > start){
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