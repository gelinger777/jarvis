package engine.internal.eventstore.service

import eventstore.server.startEventStoreService
import util.app

internal fun main(args: Array<String>) {
    startEventStoreService(
            path = System.getProperty("java.io.tmpdir"),
            port = 9151
    )

    app.log.info("enter to terminate")
    readLine()
    app.exit()
}