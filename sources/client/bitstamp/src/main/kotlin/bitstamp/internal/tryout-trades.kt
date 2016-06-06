package bitstamp.internal

import bitstamp.Bitstamp
import common.global.compact
import common.global.pair

fun main(args: Array<String>) {


    Bitstamp()
            .market(pair("btc", "usd"))
            .trades()
            .map { it.compact(showTime = true) }
            .subscribe { println(it) }


    readLine()
}