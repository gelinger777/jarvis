package collector.bitfinex

import bitfinex.Bitfinex
import collector.common.server.CollectorService
import eventstore.client.EventStoreClient
import proto.bitfinex.ProtoBitfinex.BitfinexCollectorConfig
import proto.common.CollectorGrpc
import util.app.log
import util.global.readConfig
import util.net

fun main(args: Array<String>) {
    log.info("starting Bitfinex collector")
    val config = BitfinexCollectorConfig.newBuilder()
            .readConfig("conf")
            .build()

    log.info("creating Bitfinex client")
    val bitfinex = Bitfinex(config.bitfinexConfig)

    log.info("connecting to EventStore")
    val eventStore = EventStoreClient(
            host = config.eventStoreConfig.host,
            port = config.eventStoreConfig.port
    )

    log.info("creating the collector")
    val collector = CollectorService(
            client = bitfinex,
            eventStore = eventStore
    )

    log.info("publishing collector via grpc")
    val server = net.grpc.server(
            config.port,
            CollectorGrpc.bindService(collector)
    ).start()

    log.info("enter to terminate")
    readLine()
    server.stop()
}
