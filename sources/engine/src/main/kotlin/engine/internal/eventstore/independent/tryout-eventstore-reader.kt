package engine.internal.eventstore.independent

import common.global.bytes
import eventstore.client.EventStoreClient
import util.app
import util.cpu

internal fun main(args: Array<String>) {

    // make sure service is started

    val client = EventStoreClient("localhost", 9151)

    val stream = client.getStream("test/tryout")

    stream.read().forEach { app.log.info("${it.index} : ${String(it.bytes())}") }

    cpu.sleep(300)
}