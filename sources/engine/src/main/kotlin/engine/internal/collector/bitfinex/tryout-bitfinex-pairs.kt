package engine.internal.collector.bitfinex

import bitfinex.Bitfinex
import common.global.compact

internal fun main(args: Array<String>) {
    Bitfinex().pairs().forEach { println(it.compact()) }

}