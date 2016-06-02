package bitstamp.internal

import bitstamp.Bitstamp
import common.global.compact
import common.global.pair
import proto.bitstamp.ProtoBitstamp
import proto.bitstamp.ProtoBitstamp.BitstampConfig

fun main(args: Array<String>) {


    Bitstamp(BitstampConfig.getDefaultInstance())
            .market(pair("btc", "usd"))
            .orders()
            .map { it.compact(showTime = true) }
            .subscribe { println(it) }


    readLine()
}