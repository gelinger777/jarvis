package common

import common.global.order
import proto.common.Order
import rx.Observable
import rx.subjects.PublishSubject
import util.MutableOption
import util.Option
import util.global.condition
import util.global.logger
import util.misc.RefCountSchTask
import java.util.*


class OrderStreamSync(val fetcher: () -> Option<OrderBatch>, val delay: Long) {

    val log by logger("orderSync")

    val stream = PublishSubject.create<Order>()
    val buffer = LinkedList<Order>()
    var isSynced = false
    val snapshot = MutableOption.empty<OrderBatch>()

    val fetcherTask = RefCountSchTask(
            name = "snapshot-fetcher",
            task = {
                log.debug("fetching")
                fetcher.invoke().ifPresent { snapshot.take(it) }
            },
            delay = delay
    )


    fun next(order: Order) {

        if (isSynced) {
            log.debug("synced, emitting")
            stream.onNext(order)
        } else {
            log.debug("not synced adding to buffer")
            println("working with ${order.time}")
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

            val (batchTime, batchOrders) = snapshot.get()

            if (buffer.first.time >= batchTime) {
                log.debug("outdated snapshot, skipping...")
            } else {
                log.debug("up to date snapshot")
                fetcherTask.forceStop()

                log.debug("emitting the snapshot")
                batchOrders.forEach { stream.onNext(it) }
                this.snapshot.clear()

                log.debug("emitting buffered orders")

                while (buffer.isNotEmpty()) {
                    val bufferedOrder = buffer.pollFirst()

                    if (bufferedOrder.time <= batchTime) {
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

data class OrderBatch(val time: Long, val orders: List<Order>)

fun main(args: Array<String>) {

    val option = MutableOption.empty<OrderBatch>()

    val supplier: () -> Option<OrderBatch> = { option.immutable() }

    val sync = OrderStreamSync(supplier, 2000)

    sync.stream.subscribe { println("ordTime : ${it.time}") }

    println("setting outdated snapshot")

    sync.next(ord(2))

    option.take(
            OrderBatch(
                    time = 1,
                    orders = listOf(
                            ord(1), ord(1)
                    )
            )
    )



    sync.next(ord(3))


    sync.next(ord(4))
    sync.next(ord(5))
    sync.next(ord(6))

    println("setting up to date snapshot")

    option.take(
            OrderBatch(
                    time = 4,
                    orders = listOf(
                            ord(4), ord(4)
                    )
            )
    )
    sync.next(ord(7))
    sync.next(ord(8))

}

fun ord(time: Long): Order {
    return order(Order.Side.ASK, 450.0, 10.0, time)
}