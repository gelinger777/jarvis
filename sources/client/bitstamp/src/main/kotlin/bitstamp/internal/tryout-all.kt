package bitstamp.internal

import bitstamp.Bitstamp
import common.global.compact
import common.global.pair

fun main(args: Array<String>) {
    val market = Bitstamp()
            .market(pair("btc", "usd"))

    market
            .orders()
            .map { it.compact(showTime = true) }
            .subscribe { println("ORDER : " + it) }

    market
            .trades()
            .map { it.compact(showTime = true) }
            .subscribe { println("TRADE : " + it) }


    readLine()
}