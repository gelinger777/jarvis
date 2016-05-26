package eventstore

import eventstore.server.EventStore
import proto.eventstore.EventStoreGrpc
import util.app
import util.net


fun main(args: Array<String>) {

    app.log.info("starting EventStoreServer")
    val server = net.grpc.server(
            port = EventStore.conf.port,
            service = EventStoreGrpc.bindService(EventStore)
    )

    server.start().blockForTermination()
}