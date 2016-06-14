package common.internal

import common.OrderStreamSync
import common.Orderbook
import common.global.order
import proto.common.Order
import util.MutableOption
import util.Option
import util.app.log
import util.cpu

internal fun main(args: Array<String>) {

    val option = MutableOption.empty<Orderbook>()

    val supplier: () -> Option<Orderbook> = { option.immutable() }

    val sync = OrderStreamSync(supplier, 2000)

    sync.stream.subscribe { log.info { "ordTime : ${it.time}" } }

    log.info { "setting outdated snapshot" }

    sync.next(ord(2))

    option.take(
            Orderbook(
                    time = 1,
                    asks = listOf(
                            ord(1), ord(1)
                    )
            )
    )



    sync.next(ord(3))


    sync.next(ord(4))
    sync.next(ord(5))
    sync.next(ord(6))

    log.info { "setting up to date snapshot" }

    option.take(
            Orderbook(
                    time = 4,
                    asks = listOf(
                            ord(4), ord(4)
                    )
            )
    )
    cpu.sleep(3000)
    sync.next(ord(7))
    sync.next(ord(8))
    sync.next(ord(9))
    sync.next(ord(10))

}

private fun ord(time: Long): Order {
    return order(Order.Side.ASK, 450.0, 10.0, time)
}