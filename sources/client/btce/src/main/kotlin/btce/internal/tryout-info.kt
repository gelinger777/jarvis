package btce.internal

import btce.Btce
import common.global.asKey
import proto.bitfinex.ProtoBtce

internal fun main(args: Array<String>) {
    val btce = Btce(ProtoBtce.BtceConfig.getDefaultInstance())

    btce.pairs().forEach { println(it.asKey()) }
}