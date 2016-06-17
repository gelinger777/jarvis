package client.btce.internal

import client.btce.Btce
import common.global.compact
import common.global.pair
import util.app

internal fun main(args: Array<String>) {

    Btce().market(pair("btc", "usd")).trades().forEach { app.log.info(it.compact()) }

    readLine()
}