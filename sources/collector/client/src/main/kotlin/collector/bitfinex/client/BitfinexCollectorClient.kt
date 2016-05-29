package collector.bitfinex.client

import common.ICollector
import proto.common.*
import rx.Observable
import rx.subjects.PublishSubject
import util.global.asGrpcObserver

class BitfinexCollectorClient(val address: ServiceAddress) : ICollector {
    private val channel = util.net.grpc.channel(address.host, address.port)
    private val asyncStub = CollectorGrpc.newStub(channel)
    private val blockingStub = CollectorGrpc.newBlockingStub(channel)

    override fun info(request: CollInfoReq): CollInfoResp {
        return blockingStub.info(request)
    }

    override fun streamTrades(request: StreamTradesReq): Observable<Trade> {
        val subject = PublishSubject.create<Trade>()
        asyncStub.streamTrades(request, subject.asGrpcObserver())
        return subject
    }

    override fun streamOrders(request: StreamOrdersReq): Observable<Order> {
        return PublishSubject.create<Order>()
                .apply { asyncStub.streamOrders(request, this.asGrpcObserver()) }
    }

    override fun recordTrades(request: RecordTradesReq): RecordTradesResp {
        return blockingStub.recordTrades(request)
    }

    override fun recordOrders(request: RecordOrdersReq): RecordOrdersResp {
        return blockingStub.recordOrders(request)
    }

    override fun streamHistoricalTrades(request: StreamHistoricalTradesReq): Observable<Trade> {
        val subject = PublishSubject.create<Trade>()
        asyncStub.streamHistoricalTrades(request, subject.asGrpcObserver())
        return subject
    }

    override fun streamHistoricalOrders(request: StreamHistoricalOrdersReq): Observable<Order> {
        val subject = PublishSubject.create<Order>()
        asyncStub.streamHistoricalOrders(request, subject.asGrpcObserver())
        return subject
    }

}