package collector.client

import proto.common.*
import rx.Observable
import rx.subjects.PublishSubject
import util.global.asGrpcObserver

class CollectorClient(val host: String, val port: Int) {
    private val channel = util.net.grpc.channel(host, port)
    private val asyncStub = CollectorGrpc.newStub(channel)
    private val blockingStub = CollectorGrpc.newBlockingStub(channel)

    fun info(): CollInfoResp {
        return blockingStub.info(CollInfoReq.getDefaultInstance())
    }

    fun streamTrades(pair: Pair): Observable<Trade> {
        return PublishSubject.create<Trade>().apply {
            asyncStub.streamTrades(
                    StreamTradesReq.newBuilder().setPair(pair).build(),
                    this.asGrpcObserver()
            )
        }
    }

    fun streamOrders(pair: Pair): Observable<Order> {
        return PublishSubject.create<Order>().apply {
            asyncStub.streamOrders(
                    StreamOrdersReq.newBuilder().setPair(pair).build(),
                    this.asGrpcObserver()
            )
        }
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

    fun streamHistoricalTrades(pair: Pair, start: Long = -1, end: Long = -1): Observable<Trade> {
        return PublishSubject.create<Trade>().apply {
            asyncStub.streamHistoricalTrades(
                    StreamHistoricalTradesReq.newBuilder()
                            .setPair(pair)
                            .setStartIndex(start)
                            .setEndIndex(end)
                            .build(),
                    this.asGrpcObserver()
            )
        }
    }

    fun streamHistoricalOrders(pair: Pair, start: Long = -1, end: Long = -1): Observable<Order> {
        return PublishSubject.create<Order>().apply {
            asyncStub.streamHistoricalOrders(
                    StreamHistoricalOrdersReq.newBuilder()
                            .setPair(pair)
                            .setStartIndex(start)
                            .setEndIndex(end)
                            .build(),
                    this.asGrpcObserver()
            )
        }
    }
}