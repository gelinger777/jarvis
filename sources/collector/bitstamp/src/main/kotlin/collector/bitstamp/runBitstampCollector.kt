package collector.bitstamp

import bitstamp.Bitstamp
import collector.common.CollectorService
import proto.bitstamp.ProtoCollector.BitstampCollectorConfig
import proto.common.CollectorGrpc
import util.app
import util.app.log
import util.cleanupTasks
import util.global.readConfig
import util.net

fun startBitstampCollectorService(port: Int) {
    log.info("creating Bitstamp client")
    val bitfinex = Bitstamp()

    log.info("creating the collector")
    val collector = CollectorService(bitfinex)

    log.info("publishing collector via grpc")
    val server = net.grpc.server(port, CollectorGrpc.bindService(collector)).start()

    cleanupTasks.add(key = "bitstamp-collector", task = { server.stop() })
}

fun main(args: Array<String>) {
    app.log.info("reading the configuration")
    val config = BitstampCollectorConfig.newBuilder().readConfig("conf").build()

    startBitstampCollectorService(config.port)

    log.info("enter to terminate")
    readLine()
    app.exit()
}