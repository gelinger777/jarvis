package engine.internal.collector.bitstamp

import bitstamp.Bitstamp
import common.global.compact
import common.global.pair

fun main(args: Array<String>) {

    Bitstamp().market(pair("btc", "usd")).trades().forEach { println(it.compact()) }

    readLine()
}