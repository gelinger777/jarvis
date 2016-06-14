package engine.internal.collector.bitstamp

import bitstamp.Bitstamp
import common.global.compact
import common.global.pair
import util.app

fun main(args: Array<String>) {

    Bitstamp().market(pair("btc", "usd")).trades().forEach { app.log.info { it.compact() } }

    readLine()
}