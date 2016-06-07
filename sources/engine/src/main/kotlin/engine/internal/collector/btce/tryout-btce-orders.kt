package engine.internal.collector.btce

import btce.Btce
import common.global.compact
import common.global.pair

internal fun main(args: Array<String>) {

    Btce().market(pair("btc", "usd")).orders().forEach { println(it.compact()) }

    readLine()
}