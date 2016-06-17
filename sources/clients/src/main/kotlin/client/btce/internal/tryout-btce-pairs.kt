package client.btce.internal

import client.btce.Btce
import common.global.compact
import util.app

internal fun main(args: Array<String>) {
    Btce().pairs().forEach { app.log.info(it.compact()) }
}