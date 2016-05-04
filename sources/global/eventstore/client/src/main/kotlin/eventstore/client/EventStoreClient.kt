package eventstore.client

import io.grpc.ManagedChannelBuilder
import proto.common.ServiceAddress
import proto.eventstore.EventStoreGrpc
import util.global.computeIfAbsent
import util.global.logger

class EventStoreClient(val address : ServiceAddress) {

    val log by logger("event-store-client")

    val channel = ManagedChannelBuilder.forAddress(address.host, address.port).usePlaintext(true).build()
    val asyncStub = EventStoreGrpc.newStub(channel)
    val blockStub = EventStoreGrpc.newBlockingStub(channel)

    val streams = mutableMapOf<String, EventStream>()

    @Synchronized fun getStream(path: String): EventStream {
        return streams.computeIfAbsent(path, { EventStream(it, this) })
    }
}

