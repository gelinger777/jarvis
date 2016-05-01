package eventstore.client

import io.grpc.ManagedChannelBuilder
import proto.eventstore.EventStoreGrpc
import util.global.computeIfAbsent
import util.global.logger

class EventStoreClient(host: String, port: Int) {

    val log by logger("event-store-client")

    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build()
    val asyncStub = EventStoreGrpc.newStub(channel)
    val blockStub = EventStoreGrpc.newBlockingStub(channel)

    val streams = mutableMapOf<String, EventStream>()

    @Synchronized fun getStream(path: String): EventStream {
        return streams.computeIfAbsent(path, { EventStream(it, this) })
    }
}

