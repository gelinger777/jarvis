package collector.bitstamp

import btce.Btce
import collector.common.CollectorService
import proto.bitstamp.ProtoCollector.BtceCollectorConfig
import proto.common.CollectorGrpc
import util.app
import util.app.log
import util.cleanupTasks
import util.global.readConfig
import util.net

fun startBtceCollectorService(port: Int) {
    log.info("creating Btce client")
    val bitfinex = Btce()

    log.info("creating the collector")
    val collector = CollectorService(bitfinex)

    log.info("publishing collector via grpc")
    val server = net.grpc.server(port, CollectorGrpc.bindService(collector)).start()

    cleanupTasks.add(key = "btce-collector", task = { server.stop() })
}

fun main(args: Array<String>) {
    app.log.info("reading the configuration")
    val config = BtceCollectorConfig.newBuilder().readConfig("conf").build()

    startBtceCollectorService(config.port)

    log.info("enter to terminate")
    readLine()
    app.exit()
}