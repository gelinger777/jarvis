package engine.internal.collector.btce

import btce.Btce
import common.global.compact

internal fun main(args: Array<String>) {
    Btce().pairs().forEach { println(it.compact()) }
}