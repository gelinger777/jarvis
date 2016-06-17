package client.bitstamp.internal

import client.bitstamp.Bitstamp
import common.global.compact
import util.app

fun main(args: Array<String>) {

    Bitstamp().pairs().forEach { app.log.info { it.compact() } }

}