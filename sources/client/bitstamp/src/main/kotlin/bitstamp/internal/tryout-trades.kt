package bitstamp.internal

import bitstamp.Bitstamp
import common.global.compact
import common.global.pair
import proto.bitfinex.ProtoBitstamp

fun main(args: Array<String>) {


    Bitstamp(ProtoBitstamp.BitstampConfig.getDefaultInstance())
            .market(pair("btc", "usd"))
            .trades()
            .map { it.compact(showTime = true) }
            .subscribe { println(it) }


    readLine()
}