package collector.bitfinex

import bitfinex.Bitfinex
import collector.common.server.CollectorService
import eventstore.client.EventStoreClient
import proto.bitfinex.ProtoBitfinex.BitfinexCollectorConfig
import proto.common.CollectorGrpc
import util.app
import util.app.log
import util.cleanupTasks
import util.global.readConfig
import util.net

fun startBitfinexCollectorService(port : Int, eventStoreHost:String, eventStorePort:Int){
    log.info("connecting to EventStore")
    val eventStore = EventStoreClient(
            host = eventStoreHost,
            port = eventStorePort
    )

    log.info("creating Bitfinex client")
    val bitfinex = Bitfinex()


    log.info("creating the collector")
    val collector = CollectorService(
            client = bitfinex,
            eventStore = eventStore
    )

    log.info("publishing collector via grpc")
    val server = net.grpc.server(
            port = port,
            service = CollectorGrpc.bindService(collector)
    ).start()

    cleanupTasks.add(
            key = "bitfinex-collector",
            task = { server.stop() }
    )
}

fun main(args: Array<String>) {
    app.log.info("reading the configuration")
    val config = BitfinexCollectorConfig.newBuilder()
            .readConfig("conf")
            .build()

    startBitfinexCollectorService(
            port = config.port,
            eventStoreHost = config.eventStoreConfig.host,
            eventStorePort = config.eventStoreConfig.port
    )

    log.info("enter to terminate")
    readLine()
    app.exit()
}
