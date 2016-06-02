package eventstore.server

import proto.eventstore.EventStoreGrpc
import proto.eventstore.ProtoES.EventStoreConfig
import util.app.log
import util.global.readConfig
import util.net


fun main(args: Array<String>) {

    log.info("starting EventStore server")
    val config = EventStoreConfig.newBuilder()
            .readConfig("config")
            .build();

    log.info("instantiating EventStore")
    val eventStore = EventStore(config.path)

    log.info("starting grpc service")
    val server = net.grpc.server(
            port = config.port,
            service = EventStoreGrpc.bindService(eventStore)
    )

    server.start().blockForTermination()
}