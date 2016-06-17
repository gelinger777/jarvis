package client.bitfinex.internal

import client.bitfinex.Bitfinex
import common.global.compact
import util.app.log

internal fun main(args: Array<String>) {
    Bitfinex().pairs().forEach { log.info { it.compact() } }
}