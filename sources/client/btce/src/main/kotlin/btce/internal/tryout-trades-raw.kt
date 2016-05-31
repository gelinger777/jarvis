package btce.internal

import com.google.protobuf.util.JsonFormat
import common.global.trade
import proto.common.Trade

internal fun main(args: Array<String>) {

     val str = "{ \"time\": \"1464674290\", \"price\": 530.0, \"volume\": 0.99802 }"

    val trade = trade(530.0, 0.99802, 1464674290)

    val bui = Trade.newBuilder()
    JsonFormat.parser().merge(str, bui)

    println(trade)
    println(bui.build())

    println(trade == bui.build())




//    pollTrades(pair("btc", "usd"), 1000)
//            .ifPresent { it.forEach { println(it.json()) } }
}
