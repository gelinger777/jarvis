package eventstore.server.internal

//package engine.internal.eventstore
//
//import eventstore.client.EventStoreClient
//import util.app
//import util.cpu
//import kotlin.concurrent.thread
//
//
//internal fun main(args: Array<String>) {
//
//    // make sure service is started
//
//    val client = EventStoreClient("localhost", 9151)
//
//    val stream = client.getStream("test/tryout")
//
//    thread(start = true, isDaemon = true, block = {
//        while (true) {
//            stream.write("ping".toByteArray())
//            cpu.sleep(300)
//        }
//    })
//
//   app.log.info("enter to terminate")
//    readLine()
//}