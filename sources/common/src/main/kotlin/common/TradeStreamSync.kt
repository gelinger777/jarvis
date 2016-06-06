package common

import com.google.common.collect.EvictingQueue
import proto.common.Trade
import rx.Observable
import rx.subjects.PublishSubject
import util.Option
import util.global.logger
import util.misc.RefCountSchTask

/**
 * This synchronization utility will query latest trades, then detect trades that were already streamed
 * and skip those, this way it converts polling api to streaming...
 *
 * By default we expect to find perfectly identical 42 trades to assume we found the matching point. (because 42 is the answer for everything...)
 */
class TradeStreamSync(val fetcher: () -> Option<List<Trade>>, val delay: Long){

    val log by logger("tradeSync")
    val subject = PublishSubject.create<Trade>()

    val fetcherTask = RefCountSchTask(
            name = "trades-fetcher",
            task = {
                log.debug("polling")
                fetcher.invoke().ifPresent {
                }
            },
            delay = delay
    )

    val evictingQueue = EvictingQueue.create<Trade>(42);

    fun stream() : Observable<Trade> {
        return subject
    }
}