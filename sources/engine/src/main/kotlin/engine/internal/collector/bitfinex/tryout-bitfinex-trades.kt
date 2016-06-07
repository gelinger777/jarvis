package engine.internal.collector.bitfinex

import bitfinex.Bitfinex
import common.global.compact
import common.global.pair

internal fun main(args: Array<String>) {

    Bitfinex().market(pair("btc", "usd")).trades().forEach { println(it.compact()) }

    readLine()

}