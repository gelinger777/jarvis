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

    val log = logger("orderSync")

    val stream = PublishSubject.create<Order>()
    val buffer = LinkedList<Order>()
    var isSynced = false
    val snapshot = MutableOption.empty<Orderbook>()

    val fetcherTask = RefCountSchTask(
            name = "order-fetcher",
            task = {
                log.debug { "polling" }
                fetcher.invoke().ifPresent {
                    log.debug { "snapshot timestamp : ${it.time}" }
                    snapshot.take(it)
                }
            },
            delay = delay
    )


    fun next(order: Order) {

        if (isSynced) {
            log.debug { "synced, emitting" }
            stream.onNext(order)
        } else {
            log.debug { "not synced adding to buffer" }
            log.debug { "order time : ${order.time}" }
            buffer.addLast(order)

            log.debug { "getting the snapshot" }

            if (!fetcherTask.isStarted()) {
                fetcherTask.increment()
            }

            val snapshot = snapshot.immutable()

            if (snapshot.isNotPresent()) {
                log.debug { "no snapshot, skipping..." }
                return
            }

            val book = snapshot.get()

            if (buffer.first.time >= book.time) {
                log.debug { "outdated snapshot, skipping..." }
            } else {

                log.info { "in sync" }

                fetcherTask.forceStop() // todo does not work...

                log.debug { "emitting the snapshot" }
                book.all().forEach { stream.onNext(it) }
                this.snapshot.clear()

                log.debug { "emitting buffered orders" }

                while (buffer.isNotEmpty()) {
                    val bufferedOrder = buffer.pollFirst()

                    if (bufferedOrder.time < book.time) {
                        log.debug { "skipping : ${bufferedOrder.time}" }
                    } else {
                        log.debug { "applying : ${bufferedOrder.time}" }
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

