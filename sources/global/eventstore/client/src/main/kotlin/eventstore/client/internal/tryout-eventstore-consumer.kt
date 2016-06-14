package eventstore.client.internal

import eventstore.client.EventStoreClient
import util.app

/**
 *
 */
internal fun main(args: Array<String>) {
    val esc = EventStoreClient(
            host = "localhost",
            port = 9151
    )

    val es = esc.getStream("demo/test")

    es.stream()
            .subscribe (
                    { app.log.info("${it.index} : ${String(it.data.toByteArray())}") },
                    { it.printStackTrace() },
                    { app.log.info("completed") }
            )


    app.log.info("enter to finish")
    readLine()
}