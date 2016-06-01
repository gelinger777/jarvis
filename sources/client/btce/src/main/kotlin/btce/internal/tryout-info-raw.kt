package btce.internal

import common.global.compact

internal fun main(args: Array<String>) {
    pollPairs()
            .get()
            .forEach { println(it.compact()) }
}