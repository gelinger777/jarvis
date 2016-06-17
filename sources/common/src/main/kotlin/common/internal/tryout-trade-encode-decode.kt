package common.internal

import common.global.decodeTrades
import common.global.encodeTrades
import common.global.protoRandom

internal fun main(args: Array<String>) {
    protoRandom.trades
            .encodeTrades()
            .decodeTrades()
            .subscribe()

    protoRandom.nextTrade()
    protoRandom.nextTrade()
    protoRandom.nextTrade()
}