package collector.bitstamp

import btce.Btce
import collector.common.CollectorService
import proto.common.CollectorGrpc
import util.app
import util.app.log
import util.cleanupTasks
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

    val port = app.prop("port").toInt()

    startBtceCollectorService(port)

    log.info("enter to terminate")
    readLine()
    app.exit()
}