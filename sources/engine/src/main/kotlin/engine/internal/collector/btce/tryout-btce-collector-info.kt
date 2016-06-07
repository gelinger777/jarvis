package engine.internal.collector.btce

import collector.bitstamp.startBtceCollectorService
import collector.client.CollectorClient
import common.global.compact
import eventstore.server.startEventStoreService

internal fun main(args: Array<String>) {


    startEventStoreService(
            path = System.getProperty("java.io.tmpdir"),
            port = 9151
    )

    startBtceCollectorService(
            port = 9154,
            eventStoreHost = "localhost",
            eventStorePort = 9151
    )

    val bitstampCollectorClient = CollectorClient("localhost", 9154)

    bitstampCollectorClient.info()
            .apply {
                println("Accessible Market Pairs")
                accessibleMarketPairsList.forEach { println(it.compact()) }
            }

}