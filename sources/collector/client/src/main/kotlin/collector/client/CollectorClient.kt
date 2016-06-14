package collector.client

import proto.common.*

class CollectorClient(val host: String, val port: Int) {
    private val channel = util.net.grpc.channel(host, port)
    private val blockingStub = CollectorGrpc.newBlockingStub(channel)

    fun info(): CollInfoResp {
        return blockingStub.info(CollInfoReq.getDefaultInstance())
    }

    fun recordTrades(pair: Pair): RecordTradesResp {
        return blockingStub.recordTrades(
                RecordTradesReq.newBuilder().setPair(pair).build()
        )
    }

    fun recordOrders(pair: Pair): RecordOrdersResp {
        return blockingStub.recordOrders(
                RecordOrdersReq.newBuilder().setPair(pair).build()
        )
    }
}