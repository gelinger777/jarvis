package bitstamp.internal

import bitstamp.Bitstamp
import common.global.compact
import common.global.pair
import proto.bitstamp.ProtoBitstamp
import proto.bitstamp.ProtoBitstamp.BitstampConfig

fun main(args: Array<String>) {
    val market = Bitstamp(BitstampConfig.getDefaultInstance())
            .market(pair("btc", "usd"))

    market
            .orders()
            .map { it.compact(showTime = true) }
            .subscribe { println("ORDER : " + it) }

    market
            .trades()
            .map { it.compact(showTime = true) }
            .subscribe { println("TRADE : " + it) }


    readLine()
}