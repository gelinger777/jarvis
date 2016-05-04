package collector.bitfinex

import collector.bitfinex.client.BitfinexCollectorClient
import common.util.address
import common.util.json
import common.util.pair
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
