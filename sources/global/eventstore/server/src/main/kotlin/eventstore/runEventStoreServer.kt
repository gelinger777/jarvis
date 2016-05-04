package eventstore

import eventstore.server.EventStore
import proto.eventstore.EventStoreGrpc
import util.app
import util.grpc.GrpcServer


fun main(args: Array<String>) {

    app.log.info("starting EventStoreServer")
    val server = GrpcServer(
            port = EventStore.conf.port,
            service = EventStoreGrpc.bindService(EventStore)
    )

    server.start().blockForTermination()
}