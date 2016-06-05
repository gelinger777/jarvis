package collector.client.internal

import collector.client.CollectorClient
import common.global.compact
import common.global.pair

/**
 * To try out bitfinex collector client you must have eventstore and bitfinex collector running.
 */
fun main(args: Array<String>) {
    val client = CollectorClient("localhost", 9152)

    client.info()
            .apply {
                println("available pairs")
                this.accessibleMarketPairsList.forEach { println(it.compact()) }
            }
            .apply {
                println("recording streams")
                this.currentStreamsList.forEach { println(it) }
            }

    readLine()

    client.recordOrders(pair("BTC", "USD"))
    readLine()

    client.info()
    .apply {
        println("available pairs")
        this.accessibleMarketPairsList.forEach { println(it.compact()) }
    }
    .apply {
        println("recording streams")
        this.currentStreamsList.forEach { println(it) }
    }

    readLine()
}
