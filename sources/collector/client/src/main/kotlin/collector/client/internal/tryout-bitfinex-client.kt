package collector.client.internal

import collector.client.CollectorClient
import common.global.json
import common.global.pair

fun main(args: Array<String>) {
    val client = CollectorClient("localhost", 9152)

    val streamOrders = client.streamOrders(pair("BTC", "USD"))

    streamOrders.subscribe { println(it.json()) }

    readLine()
}
