package common.internal

import common.global.compact
import common.global.decodeTrades
import common.global.encodeTrades
import common.global.protoRandom
import util.app

internal fun main(args: Array<String>) {

    protoRandom.trades
            .doOnNext({ app.log.info { "encoding : ${it.compact()}" } })
            .encodeTrades()
            .doOnNext({ app.log.debug { "${it.size} bytes" } })
            .decodeTrades()
            .forEach {
                app.log.info { "decoding : ${it.compact()}" }
            }

    protoRandom.nextTrade()
    protoRandom.nextTrade()
    protoRandom.nextTrade()

}