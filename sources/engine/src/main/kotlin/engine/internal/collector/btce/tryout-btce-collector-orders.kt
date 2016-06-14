package engine.internal.collector.btce

import collector.bitstamp.startBtceCollectorService
import collector.client.CollectorClient
import common.global.compact
import common.global.pair
import eventstore.server.startEventStoreService
import util.app

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

    bitstampCollectorClient.recordOrders(pair("btc", "usd"))

    readLine()

    bitstampCollectorClient.streamHistoricalOrders(pair("btc", "usd"))
            .forEach { app.log.info(it.compact()) }

    readLine()


}