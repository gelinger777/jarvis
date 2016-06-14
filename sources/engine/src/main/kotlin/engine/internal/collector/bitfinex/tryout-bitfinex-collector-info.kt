package engine.internal.collector.bitfinex

import collector.bitfinex.startBitfinexCollectorService
import collector.client.CollectorClient
import common.global.compact
import eventstore.server.startEventStoreService
import util.app.log

internal fun main(args: Array<String>) {


    startEventStoreService(
            path = System.getProperty("java.io.tmpdir"),
            port = 9151
    )

    startBitfinexCollectorService(
            port = 9152,
            eventStoreHost = "localhost",
            eventStorePort = 9151
    )

    val bitfinexCollectorClient = CollectorClient("localhost", 9152)

    bitfinexCollectorClient.info()
            .apply {
                log.info { "Accessible Market Pairs" }
                accessibleMarketPairsList.forEach { log.info { it.compact() } }
            }

}