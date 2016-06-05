package collector.client.internal

import collector.client.CollectorClient

fun main(args: Array<String>) {
    val client = CollectorClient("localhost", 9153)

//    val streamOrders = client.streamOrders(pair("BTC", "USD"))

//    streamOrders.subscribe { println(it.json()) }

    readLine()
}
