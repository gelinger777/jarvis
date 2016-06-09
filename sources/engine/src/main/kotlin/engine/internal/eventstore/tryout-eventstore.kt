package engine.internal.eventstore

import common.global.bytes
import eventstore.client.EventStoreClient
import eventstore.server.startEventStoreService
import util.global.logger

internal fun main(args: Array<String>) {

    startEventStoreService(
            path = System.getProperty("java.io.tmpdir"),
            port = 9151
    )

    val client = EventStoreClient("localhost", 9151)
    val stream = client.getStream("test/tryout")
    val log = logger("test")

    for(i in 0..10){
        stream.write("ping".toByteArray())
    }

    readLine()

    stream.read().forEach { log.info("${it.index} : ${String(it.bytes())}") }

    readLine()


}