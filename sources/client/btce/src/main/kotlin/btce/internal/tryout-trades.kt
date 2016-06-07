package btce.internal

import common.global.compact
import common.global.pair

internal fun main(args: Array<String>) {

    btce.Btce().market(pair("btc", "usd")).trades().forEach { println(it.compact()) }

    readLine()
}