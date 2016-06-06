package engine.internal.collector.bitfinex

import collector.bitfinex.startBitfinexCollectorService
import collector.client.CollectorClient
import common.global.compact
import common.global.pair
import common.global.parseTrade
import eventstore.client.EventStoreClient
import eventstore.server.startEventStoreService

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


    bitfinexCollectorClient.recordTrades(pair("btc", "usd"))

    // directly read from eventstore as trades being written
    eventStoreClient.getStream("bitfinex/btc-usd/trades")
            .stream()
            .forEach {
                println(it.parseTrade().compact(showTime = true))
            }

    readLine()

}