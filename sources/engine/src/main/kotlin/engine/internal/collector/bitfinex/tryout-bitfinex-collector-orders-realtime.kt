package engine.internal.collector.bitfinex

import collector.bitfinex.startBitfinexCollectorService
import collector.client.CollectorClient
import common.global.compact
import common.global.pair
import common.global.parseOrder
import eventstore.client.EventStoreClient
import eventstore.server.startEventStoreService
import util.app

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


    val eventStoreClient = EventStoreClient("localhost", 9151)

    val bitfinexCollectorClient = CollectorClient("localhost", 9152)


    bitfinexCollectorClient.recordOrders(pair("btc", "usd"))

    // directly read from eventstore as orders being written
    eventStoreClient.getStream("bitfinex/btc-usd/orders")
            .stream()
            .forEach {
                app.log.info { it.parseOrder().compact() }
            }

    readLine()

}