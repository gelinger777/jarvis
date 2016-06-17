package client.bitfinex.internal

import client.bitfinex.Bitfinex
import common.global.compact
import common.global.pair
import util.app

internal fun main(args: Array<String>) {

    Bitfinex().market(pair("btc", "usd")).trades().forEach { app.log.info { it.compact() } }

    readLine()
}