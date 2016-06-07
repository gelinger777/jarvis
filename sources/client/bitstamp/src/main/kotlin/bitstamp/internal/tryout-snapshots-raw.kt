package bitstamp.internal

import common.global.pair
import util.misc.RefCountSchTask

fun main(args: Array<String>) {

    val pair = pair("btc", "usd")

    RefCountSchTask(
            name = "shapshots",
            task = { getOrderbookSnapshot(pair).apply { this.ifPresent { println(it) } } },
            delay = 2000
    ).increment()

    readLine()
}