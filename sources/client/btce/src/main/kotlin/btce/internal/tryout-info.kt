package btce.internal

import common.global.asKey

internal fun main(args: Array<String>) {
    pollPairs()
            .get()
            .forEach { println(it.asKey()) }
}