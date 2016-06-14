package eventstore.server.internal

//package engine.internal.eventstore
//
//import common.global.bytes
//import eventstore.client.EventStoreClient
//import util.app
//
//internal fun main(args: Array<String>) {
//
//    // make sure service is started
//    val client = EventStoreClient("localhost", 9151)
//
//    client.getStream("test/tryout")
//            .stream().forEach { app.log.info("${it.index} : ${String(it.bytes())}") }
//
//
//   app.log.info("enter to terminate")
//    readLine()
//}