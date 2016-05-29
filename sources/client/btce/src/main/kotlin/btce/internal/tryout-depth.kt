package btce.internal

import common.global.all
import common.global.json
import common.global.pair

internal fun main(args: Array<String>) {
    pollOrders(pair("btc", "usd"))
            .ifPresent {
                it.all().forEach { println(it.json()) }
            }
    .ifNotPresentWTF() // todo im here

}