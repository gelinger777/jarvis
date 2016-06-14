package engine.internal.collector.bitstamp

import collector.bitstamp.startBitstampCollectorService
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

    startBitstampCollectorService(
            port = 9153,
            eventStoreHost = "localhost",
            eventStorePort = 9151
    )

    val bitstampCollectorClient = CollectorClient("localhost", 9153)

    bitstampCollectorClient.recordTrades(pair("btc", "usd"))

    readLine()

    bitstampCollectorClient.streamHistoricalTrades(pair("btc", "usd"))
            .forEach { app.log.info { it.compact() } }

    readLine()


}