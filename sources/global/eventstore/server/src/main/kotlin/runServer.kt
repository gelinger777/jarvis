import eventstore.server.EventStore
import proto.eventstore.EventStoreGrpc
import util.grpc.GrpcServer


fun main(args: Array<String>) {
    val server = GrpcServer(
            port = EventStore.conf.port,
            service = EventStoreGrpc.bindService(EventStore)
    )

    server.start().blockForTermination()
}