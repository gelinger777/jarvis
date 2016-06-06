package bitstamp.internal

import common.global.compact
import util.global.filterEmptyOptionals

internal fun main(args: Array<String>) {
    util.net.pusher.stream("de504dc5763aeef9ff52", "live_trades", "trade")
            .map { parseTrade(it) }
            .filterEmptyOptionals()
            .map { it.compact(showTime = true) }
            .subscribe { println(it) }

    readLine()
}
