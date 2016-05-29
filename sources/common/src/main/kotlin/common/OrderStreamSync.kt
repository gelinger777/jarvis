package common

import proto.common.Order
import rx.Observable
import rx.subjects.PublishSubject
import util.MutableOption
import util.Option
import util.global.condition
import util.global.logger
import util.misc.RefCountSchTask
import java.util.*


class OrderStreamSync(val fetcher: () -> Option<Orderbook>, val delay: Long) {

    val log by logger("orderSync")

    val stream = PublishSubject.create<Order>()
    val buffer = LinkedList<Order>()
    var isSynced = false
    val snapshot = MutableOption.empty<Orderbook>()

    val fetcherTask = RefCountSchTask(
            name = "snapshot-fetcher",
            task = {
                log.debug("fetching")
                fetcher.invoke().ifPresent {
                    log.debug("snapshot timestamp : ${it.time}")
                    snapshot.take(it)
                }
            },
            delay = delay
    )


    fun next(order: Order) {

        if (isSynced) {
            log.debug("synced, emitting")
            stream.onNext(order)
        } else {
            log.debug("not synced adding to buffer")
            log.debug("working with ${order.time}")
            buffer.addLast(order)

            log.debug("getting the snapshot")

            if(!fetcherTask.isStarted()){
                fetcherTask.forceStart()
            }

            val snapshot = snapshot.immutable()

            if (snapshot.isNotPresent()) {
                log.debug("no snapshot, skipping...")
                return
            }

            val book = snapshot.get()

            if (buffer.first.time >= book.time) {
                log.debug("outdated snapshot, skipping...")
            } else {
                log.debug("up to date snapshot")
                fetcherTask.forceStop()

                log.debug("emitting the snapshot")
                book.all().forEach { stream.onNext(it) }
                this.snapshot.clear()

                log.debug("emitting buffered orders")

                while (buffer.isNotEmpty()) {
                    val bufferedOrder = buffer.pollFirst()

                    if (bufferedOrder.time < book.time) {
                        log.debug("skipping : ${bufferedOrder.time}")
                    } else {
                        log.debug("applying : ${bufferedOrder.time}")
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

