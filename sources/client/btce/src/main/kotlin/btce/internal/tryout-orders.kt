package btce.internal

import btce.Btce
import common.global.json
import common.global.pair
import proto.bitfinex.ProtoBtce

internal fun main(args: Array<String>) {

    val btce = Btce(ProtoBtce.BtceConfig.getDefaultInstance())

    btce.market(pair("btc", "usd")).orders().subscribe { println(it.json()) }

    readLine()
}