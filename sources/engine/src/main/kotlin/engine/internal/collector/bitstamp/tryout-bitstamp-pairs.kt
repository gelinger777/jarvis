package engine.internal.collector.bitstamp

import bitstamp.Bitstamp
import common.global.compact

fun main(args: Array<String>) {

    Bitstamp().pairs().forEach { println(it.compact()) }
}