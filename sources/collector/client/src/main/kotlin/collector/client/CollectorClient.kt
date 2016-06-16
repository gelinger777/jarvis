package collector.client

import proto.common.*

class CollectorClient(val host: String, val port: Int) {
    private val channel = util.net.grpc.channel(host, port)
    private val blockingStub = CollectorGrpc.newBlockingStub(channel)

    fun info(): CollInfoResp {
        return blockingStub.info(Empty.getDefaultInstance())
    }

    fun recordTrades(pair: Pair) {
        blockingStub.record(
                RecordReq.newBuilder()
                        .setType(RecordReq.Type.TRADES)
                        .setPair(pair)
                        .build()
        )
    }
    fun recordOrders(pair: Pair) {
        blockingStub.record(
                RecordReq.newBuilder()
                        .setType(RecordReq.Type.TRADES)
                        .setPair(pair)
                        .build()
        )
    }
}