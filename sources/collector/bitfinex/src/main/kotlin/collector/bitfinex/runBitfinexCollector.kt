package collector.bitfinex

import bitfinex.Bitfinex
import collector.common.CollectorService
import proto.common.CollectorGrpc
import util.app
import util.app.log
import util.maid
import util.net

fun startBitfinexCollectorService(port: Int, storeRoot: String) {
    log.info("creating Bitfinex client")
    val bitfinex = Bitfinex()

    log.info("creating the collector")
    val collector = CollectorService(bitfinex, storeRoot)

    log.info("publishing collector via grpc")
    val server = net.grpc.server(port, CollectorGrpc.bindService(collector)).start()

    maid.add(key = "bitfinex-collector", task = { server.stop() })
}

fun main(args: Array<String>) {
    app.log.info("reading the configuration")

    val port = app.prop("port").toInt()
    val storeRoot = app.prop("storeRoot")

    startBitfinexCollectorService(port, storeRoot)

    log.info("enter to terminate")
    readLine()
    app.exit()
}
