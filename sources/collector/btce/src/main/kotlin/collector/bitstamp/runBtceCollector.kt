package collector.bitstamp

import btce.Btce
import collector.common.server.CollectorService
import eventstore.client.EventStoreClient
import proto.bitstamp.ProtoCollector.BtceCollectorConfig
import proto.common.CollectorGrpc
import util.app
import util.app.log
import util.cleanupTasks
import util.global.readConfig
import util.net

fun startBtceCollectorService(port :Int, eventStoreHost:String, eventStorePort:Int){
    log.info("connecting to EventStore")
    val eventStore = EventStoreClient(
            host = eventStoreHost,
            port = eventStorePort
    )

    log.info("creating Btce client")
    val bitstamp = Btce()


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
    val config = BtceCollectorConfig.newBuilder()
            .readConfig("conf")
            .build()

    startBtceCollectorService(
            port = config.port,
            eventStoreHost = config.eventStoreConfig.host,
            eventStorePort = config.eventStoreConfig.port
    )


    log.info("enter to terminate")
    readLine()
    app.exit()
}