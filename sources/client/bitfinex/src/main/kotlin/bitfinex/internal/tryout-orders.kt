package bitfinex.internal

import bitfinex.Bitfinex
import common.global.compact
import common.global.pair

internal fun main(args: Array<String>) {
    val bitfinex = Bitfinex()

    val market = bitfinex.market(pair("btc", "usd"))

    market.orders().subscribe { println("order : " + it.compact(showTime = true)) }

    readLine()

}