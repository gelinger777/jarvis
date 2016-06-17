package common.internal

import common.global.decodeOrders
import common.global.encodeOrders
import common.global.protoRandom

internal fun main(args: Array<String>) {

    protoRandom.orders
            .encodeOrders()
            .decodeOrders()
            .subscribe()

    protoRandom.nextOrder()
    protoRandom.nextOrder()
    protoRandom.nextOrder()
    protoRandom.nextOrder()
}