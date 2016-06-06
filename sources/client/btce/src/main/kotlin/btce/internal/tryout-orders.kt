package btce.internal

import btce.Btce
import common.global.json
import common.global.pair

internal fun main(args: Array<String>) {

    Btce().market(pair("btc", "usd"))
            .orders()
            .forEach { println(it.json()) }

    readLine()
}