package collector.bitstamp

import bitstamp.Bitstamp
import collector.common.server.CollectorService
import eventstore.client.EventStoreClient
import proto.bitstamp.ProtoCollector.BitstampCollectorConfig
import proto.common.CollectorGrpc
import util.app
import util.app.log
import util.cleanupTasks
import util.global.readConfig
import util.net

fun startBitstampCollectorService(port :Int, eventStoreHost:String, eventStorePort:Int){
    log.info("connecting to EventStore")
    val eventStore = EventStoreClient(
            host = eventStoreHost,
            port = eventStorePort
    )

    log.info("creating Bitstamp client")
    val bitstamp = Bitstamp()


    log.info("creating the collector")
    val collector = CollectorService(
            client = bitstamp,
            eventStore = eventStore
    )

    log.info("publishing collector via grpc")
    val server = net.grpc.server(
            port = port,
            service = CollectorGrpc.bindService(collector)
    ).start()

    cleanupTasks.add(
            key = "bitstamp-collector",
            task = { server.stop() }
    )
}

fun main(args: Array<String>) {
    app.log.info("reading the configuration")
    val config = BitstampCollectorConfig.newBuilder()
            .readConfig("conf")
            .build()

    startBitstampCollectorService(
            port = config.port,
            eventStoreHost = config.eventStoreConfig.host,
            eventStorePort = config.eventStoreConfig.port
    )


    log.info("enter to terminate")
    readLine()
    app.exit()
}