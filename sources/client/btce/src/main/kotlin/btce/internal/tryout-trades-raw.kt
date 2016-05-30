package btce.internal

import common.global.json
import common.global.pair

internal fun main(args: Array<String>) {
    pollTrades(pair("btc", "usd"), 1000)
            .ifPresent { it.forEach { println(it.json()) } }
}
