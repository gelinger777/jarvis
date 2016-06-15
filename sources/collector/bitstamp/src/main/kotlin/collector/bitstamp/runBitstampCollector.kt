package collector.bitstamp

import bitstamp.Bitstamp
import collector.common.CollectorService
import proto.common.CollectorGrpc
import util.app
import util.app.log
import util.maid
import util.net

fun startBitstampCollectorService(port: Int, storeRoot: String) {
    log.info("creating Bitstamp client")
    val bitfinex = Bitstamp()

    log.info("creating the collector")
    val collector = CollectorService(bitfinex, storeRoot)

    log.info("publishing collector via grpc")
    val server = net.grpc.server(port, CollectorGrpc.bindService(collector)).start()

    maid.add(key = "bitstamp-collector", task = { server.stop() })
}

fun main(args: Array<String>) {
    app.log.info("reading the configuration")

    val port = app.prop("port").toInt()
    val storeRoot = app.prop("storeRoot")

    startBitstampCollectorService(port, storeRoot)

    log.info("enter to terminate")
    readLine()
    app.exit()
}