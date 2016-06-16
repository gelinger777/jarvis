package collector.bitfinex

import bitfinex.Bitfinex
import collector.common.startCollectorFor
import util.app
import util.global.sleepUntilInterrupted

fun main(args: Array<String>) {
    app.log.info { "starting Bitfinex collector" }
    startCollectorFor(Bitfinex())
    sleepUntilInterrupted()
}