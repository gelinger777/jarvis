package collector.bitstamp

//fun startBtceCollectorService(port: Int, storeRoot: String) {
//    log.info("creating Btce client")
//    val bitfinex = Btce()
//
//    log.info("creating the collector")
//    val collector = CollectorService(bitfinex, storeRoot)
//
//    log.info("publishing collector via grpc")
//    val server = net.grpc.server(port, CollectorGrpc.bindService(collector)).start()
//
//    maid.add(key = "btce-collector", task = { server.stop() })
//}
//
//fun main(args: Array<String>) {
//    app.log.info("reading the configuration")
//
//    val port = app.prop("port").toInt()
//    val storeRoot = app.prop("storeRoot")
//
//    startBtceCollectorService(port, storeRoot)
//
//    log.info("enter to terminate")
//    readLine()
//    app.exit()
//}