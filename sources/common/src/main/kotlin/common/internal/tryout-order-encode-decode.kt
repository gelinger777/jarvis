package common.internal

import common.global.compact
import common.global.decodeOrders
import common.global.encodeOrders
import common.global.protoRandom
import util.app

internal fun main(args: Array<String>) {

    protoRandom.orders
            .doOnNext({ app.log.info { "encoding : ${it.compact()}" } })
            .encodeOrders()
            .doOnNext({ app.log.debug { "${it.size} bytes" } })
            .decodeOrders()
            .forEach {
                app.log.info { "decoding : ${it.compact()}" }
            }

    protoRandom.nextOrder()
    protoRandom.nextOrder()
    protoRandom.nextOrder()
    protoRandom.nextOrder()
}