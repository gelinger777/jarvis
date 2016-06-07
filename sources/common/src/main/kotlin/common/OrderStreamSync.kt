package common

import common.global.all
import proto.common.Order
import rx.Observable
import rx.subjects.PublishSubject
import util.MutableOption
import util.Option
import util.global.condition
import util.global.logger
import util.misc.RefCountSchTask
import java.util.*


/**
 * This synchronization utility takes a fetching logic that polls a snapshot of orderbook,
 * and accepts realtime orders using next() method. It will keep polling snapshot until its
 * newer than the oldest buffered realtime order. Then it will stream the snapshot then the buffered orders,
 * and after that the actual realtime stream...
 *
 * Idiotic exchanges like Bitstamp, will have order misses so
 */
class OrderStreamSync(val fetcher: () -> Option<Orderbook>, val delay: Long) {

    val log by logger("orderSync")

    val stream = PublishSubject.create<Order>()
    val buffer = LinkedList<Order>()
    var isSynced = false
    val snapshot = MutableOption.empty<Orderbook>()

    val fetcherTask = RefCountSchTask(
            name = "order-fetcher",
            task = {
                log.trace("polling")
                fetcher.invoke().ifPresent {
                    log.trace("snapshot timestamp : ${it.time}")
                    snapshot.take(it)
                }
            },
            delay = delay
    )


    fun next(order: Order) {

        if (isSynced) {
            log.trace("synced, emitting")
            stream.onNext(order)
        } else {
            log.trace("not synced adding to buffer")
            log.trace("working with ${order.time}")
            buffer.addLast(order)

            log.trace("getting the snapshot")

            if(!fetcherTask.isStarted()){
                fetcherTask.increment()
            }

            val snapshot = snapshot.immutable()

            if (snapshot.isNotPresent()) {
                log.trace("no snapshot, skipping...")
                return
            }

            val book = snapshot.get()

            if (buffer.first.time >= book.time) {
                log.trace("outdated snapshot, skipping...")
            } else {
                log.trace("up to date snapshot")
                fetcherTask.forceStop()

                log.trace("emitting the snapshot")
                book.all().forEach { stream.onNext(it) }
                this.snapshot.clear()

                log.trace("emitting buffered orders")

                while (buffer.isNotEmpty()) {
                    val bufferedOrder = buffer.pollFirst()

                    if (bufferedOrder.time < book.time) {
                        log.trace("skipping : ${bufferedOrder.time}")
                    } else {
                        log.trace("applying : ${bufferedOrder.time}")
                        stream.onNext(bufferedOrder)
                    }
                }

                isSynced = true
                condition(buffer.isEmpty())
            }
        }


    }

    fun stream(): Observable<Order> {
        return stream
    }

}

