package engine.internal.collector.bitfinex

import bitfinex.Bitfinex
import common.global.compact
import common.global.pair
import util.app.log

internal fun main(args: Array<String>) {

    Bitfinex().market(pair("btc", "usd")).orders().forEach { log.info { it.compact() } }

    readLine()
}