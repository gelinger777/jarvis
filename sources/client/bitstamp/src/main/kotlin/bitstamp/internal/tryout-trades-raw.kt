package bitstamp.internal

import common.global.compact
import util.global.filterOptions

internal fun main(args: Array<String>) {
    util.net.pusher.stream("de504dc5763aeef9ff52", "live_trades", "trade")
            .map { parseTrade(it) }
            .filterOptions()
            .map { it.compact(showTime = true) }
            .subscribe { println(it) }

    readLine()
}
