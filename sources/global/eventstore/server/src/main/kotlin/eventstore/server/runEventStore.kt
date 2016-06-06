package eventstore.server

import proto.eventstore.EventStoreGrpc
import proto.eventstore.ProtoES.EventStoreConfig
import util.app
import util.cleanupTasks
import util.global.logger
import util.global.readConfig
import util.net


fun startEventStoreService(path: String, port: Int) {
    val log = logger("event-store-service")

    log.info("starting EventStore server")
    log.debug("data root is at : $path")
    val eventStore = EventStore(path)

    log.info("starting grpc service")
    val server = net.grpc.server(
            port = port,
            service = EventStoreGrpc.bindService(eventStore)
    ).start()

    cleanupTasks.add(
            key = "event-store",
            task = { server.stop() }
    )
}

fun main(args: Array<String>) {
    app.log.info("reading the configuration")
    val config = EventStoreConfig.newBuilder()
            .readConfig("conf")
            .build();

    startEventStoreService(
            path = config.path,
            port = config.port
    )

    app.log.info("enter to terminate")
    readLine()
    app.exit()
}