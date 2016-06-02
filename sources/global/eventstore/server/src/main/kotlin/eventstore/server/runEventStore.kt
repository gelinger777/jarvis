package eventstore.server

import proto.eventstore.EventStoreGrpc
import proto.eventstore.ProtoES.EventStoreConfig
import util.app.log
import util.global.readConfig
import util.net


fun main(args: Array<String>) {

    log.info("starting EventStore server")
    val config = EventStoreConfig.newBuilder()
            .readConfig("conf")
            .build();

    log.info("instantiating EventStore")
    log.debug("data root is at : ${config.path}")
    val eventStore = EventStore(config.path)

    log.info("starting grpc service")
    val server = net.grpc.server(
            port = config.port,
            service = EventStoreGrpc.bindService(eventStore)
    )

    log.info("enter to terminate")
    readLine()
    server.stop()
}