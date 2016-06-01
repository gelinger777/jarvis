package bitstamp.internal

import common.global.pair
import util.misc.RefCountSchTask

fun main(args: Array<String>) {

    val pair = pair("btc", "usd")
    RefCountSchTask("shapshots", { getOrderbookSnapshot(pair).apply { this.ifPresent { println(it) } } }, 2000).increment()

    readLine()
}