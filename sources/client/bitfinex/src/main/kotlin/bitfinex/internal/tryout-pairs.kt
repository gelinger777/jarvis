package bitfinex.internal

import bitfinex.Bitfinex
import common.global.compact

internal fun main(args: Array<String>) {
    val bitfinex = Bitfinex()

    bitfinex.pairs().forEach { println(it.compact()) }

}