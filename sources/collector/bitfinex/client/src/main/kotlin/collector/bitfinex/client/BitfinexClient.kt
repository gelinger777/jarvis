package collector.bitfinex.client

import common.IExchangeCollector
import io.grpc.ManagedChannelBuilder
import proto.common.*
import rx.Observable
import rx.subjects.PublishSubject
import util.cpu
import util.global.asGrpcObserver

class BitfinexClient(val host: String, val port: Int) : IExchangeCollector {
    private val channel = ManagedChannelBuilder
            .forAddress(host, port)
            .executor(cpu.executors.io)
            .build()

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
        val subject = PublishSubject.create<Order>()
        asyncStub.streamOrders(request, subject.asGrpcObserver())
        return subject
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