package collector.bitstamp

import bitstamp.Bitstamp
import collector.common.server.CollectorService
import eventstore.client.EventStoreClient
import proto.bitstamp.ProtoBitstamp.BitstampCollectorConfig
import proto.common.CollectorGrpc
import util.app.log
import util.global.readConfig
import util.net

fun main(args: Array<String>) {
    log.info("starting Bitstamp collector")
    val config = BitstampCollectorConfig.newBuilder()
            .readConfig("conf")
            .build()

    log.info("creating Bitstamp client")
    val bitstamp = Bitstamp(config.bitstampConfig)

    log.info("connecting to EventStore")
    val eventStore = EventStoreClient(
            host = config.eventStoreConfig.host,
            port = config.eventStoreConfig.port
    )

    log.info("creating the collector")
    val collector = CollectorService(
            client = bitstamp,
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