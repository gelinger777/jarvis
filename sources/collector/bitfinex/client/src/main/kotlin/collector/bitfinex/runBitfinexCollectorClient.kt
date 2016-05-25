package collector.bitfinex

import collector.bitfinex.client.BitfinexCollectorClient
import common.global.address
import common.global.json
import common.global.pair
import proto.common.StreamOrdersReq

fun main(args: Array<String>) {
    val client = BitfinexCollectorClient(address("localhost", 9152))

    val streamOrders = client.streamOrders(
            StreamOrdersReq.newBuilder()
                    .setPair(pair("BTC", "USD"))
                    .build()
    )

    streamOrders.subscribe { println(it.json()) }

    readLine()
}
