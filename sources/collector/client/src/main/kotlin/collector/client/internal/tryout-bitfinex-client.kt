package collector.client.internal

import collector.client.CollectorClient
import common.global.pair

fun main(args: Array<String>) {
    val client = CollectorClient("localhost", 9152)

    val streamOrders = client.recordOrders(pair("BTC", "USD")) // todo i'm here : need to test each collector

    readLine()
}
