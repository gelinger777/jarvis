package engine.internal.collector.bitstamp

import collector.bitstamp.startBitstampCollectorService
import collector.client.CollectorClient
import common.global.compact
import common.global.pair
import eventstore.server.startEventStoreService

internal fun main(args: Array<String>) {


    startEventStoreService(
            path = System.getProperty("java.io.tmpdir"),
            port = 9151
    )

    startBitstampCollectorService(
            port = 9152,
            eventStoreHost = "localhost",
            eventStorePort = 9151
    )

    val bitstampCollectorClient = CollectorClient("localhost", 9152)

    bitstampCollectorClient.recordTrades(pair("btc", "usd"))

    readLine()

    bitstampCollectorClient.streamHistoricalTrades(pair("btc", "usd"))
            .forEach { println(it.compact(showTime = true)) }

    readLine()


}