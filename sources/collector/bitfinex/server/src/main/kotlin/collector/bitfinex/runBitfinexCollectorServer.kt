package collector.bitfinex

import bitfinex.Bitfinex
import collector.bitfinex.server.BitfinexCollectorService
import eventstore.client.EventStoreClient
import proto.bitfinex.ProtoBitfinex.BitfinexCollectorConfig
import proto.common.CollectorGrpc
import util.app
import util.global.readFromFS
import util.net

fun main(args: Array<String>) {
    app.log.info("starting BitfinexCollectorServer")
    val config = BitfinexCollectorConfig.newBuilder()
            .readFromFS("bitfinexCollectorConfig")
            .build()

    app.log.info("instantiating BitfinexClient")
    val bitfinex = Bitfinex(config.bitfinexConfig)

    app.log.info("instantiating EventStoreClient")
    val eventStore = EventStoreClient(config.eventStoreConfig)

    app.log.info("starting grpc service")
    val bitfinexService = BitfinexCollectorService(config, bitfinex, eventStore)
    val grpcServer = net.grpcServer(config.port, CollectorGrpc.bindService(bitfinexService))
    grpcServer.start().blockForTermination()
}
