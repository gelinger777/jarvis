package common.internal

import common.OrderBatch
import common.OrderStreamSync
import common.global.order
import proto.common.Order
import util.MutableOption
import util.Option
import util.cpu

internal fun main(args: Array<String>) {

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
    cpu.sleep(3000)
    sync.next(ord(7))
    sync.next(ord(8))

}

internal fun ord(time: Long): Order {
    return order(Order.Side.ASK, 450.0, 10.0, time)
}