package eventstore.client

import proto.eventstore.EventStoreGrpc
import util.global.computeIfAbsent

class EventStoreClient(val host: String, val port: Int) {

    private val channel = util.net.grpc.channel(host, port)
    private val streams = mutableMapOf<String, EventStream>()

    internal val asyncStub = EventStoreGrpc.newStub(channel)
    internal val blockStub = EventStoreGrpc.newBlockingStub(channel)

    @Synchronized fun getStream(path: String): EventStream {
        return streams.computeIfAbsent(path, { EventStream(it, this) })
    }
}

