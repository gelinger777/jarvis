package collector.bitfinex

import bitfinex.Bitfinex
import collector.common.server.CollectorService
import eventstore.client.EventStoreClient
import proto.bitfinex.ProtoBitfinex.BitfinexCollectorConfig
import proto.common.CollectorGrpc
import util.app.log
import util.global.readFromFS

fun main(args: Array<String>) {
    log.info("starting Bitfinex collector")
    val config = BitfinexCollectorConfig.newBuilder()
            .readFromFS("bitfinexCollectorConfig")
            .build()

    log.info("instantiating BitfinexClient")
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

    log.info("starting grpc service")
    val grpcServer = util.net.grpc.server(config.port, CollectorGrpc.bindService(collector))

    grpcServer.start().blockForTermination()
}
