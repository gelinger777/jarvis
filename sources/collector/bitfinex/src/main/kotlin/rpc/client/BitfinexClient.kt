package rpc.client

import common.GenericCollector
import io.grpc.ManagedChannelBuilder
import proto.*
import rx.Observable
import util.cpu

class BitfinexClient(val host: String, val port: Int) : GenericCollector {


    private var channel = ManagedChannelBuilder
            .forAddress(host, port)
            .executor(cpu.executors.io)
            .build()

    private var asyncStub = CollectorGrpc.newStub(channel)
    private var blockingStub = CollectorGrpc.newBlockingStub(channel)


    override fun status(request: CollStatusReq): CollStatusResp {
        throw UnsupportedOperationException()
    }

    override fun shutdown(request: CollShutdownReq): CollShutdownResp {
        throw UnsupportedOperationException()
    }

    override fun streamTrades(request: StreamTradesReq): Observable<Trade> {
        throw UnsupportedOperationException()
    }

    override fun streamOrders(request: StreamOrdersReq): Observable<Order> {
        throw UnsupportedOperationException()
    }

    override fun recordTrades(request: RecordTradesReq): RecordTradesResp {
        throw UnsupportedOperationException()
    }

    override fun recordOrders(request: RecordOrdersReq): RecordOrdersResp {
        throw UnsupportedOperationException()
    }

    override fun streamHistoricalTrades(request: StreamHistoricalTradesReq): Observable<Trade> {
        throw UnsupportedOperationException()
    }

    override fun streamHistoricalOrders(request: StreamHistoricalOrdersReq): Observable<Order> {
        throw UnsupportedOperationException()
    }


    //    override fun getSystemInfo(type: SystemInfoRequest.Type): SystemInfoResponse {
    //        return blockingStub.getSystemInfo(systemInfoRequest(type))
    //    }
    //
    //    override fun shutDown(): ExecutionStatus {
    //        return blockingStub.shutDown(empty())
    //    }
    //
    //    override fun streamTrades(pair: Pair): Observable<Trade> {
    //        val subject = PublishSubject.create<Trade>()
    //        asyncStub.streamTrades(pair, subject.asGrpcObserver())
    //        return subject
    //    }
    //
    //    override fun streamOrders(pair: Pair): Observable<Order> {
    //        val subject = PublishSubject.create<Order>()
    //        asyncStub.streamOrders(pair, subject.asGrpcObserver())
    //        return subject
    //    }
    //
    //
    //    override fun startRecordingTrades(pair: Pair): ExecutionStatus {
    //        return blockingStub.startRecordingTrades(pair)
    //    }
    //
    //    override fun stopRecordingTrades(pair: Pair): ExecutionStatus {
    //        return blockingStub.stopRecordingTrades(pair)
    //    }
    //
    //
    //    override fun startRecordingOrders(pair: Pair): ExecutionStatus {
    //        return blockingStub.startRecordingOrders(pair)
    //    }
    //
    //    override fun stopRecordingOrders(pair: Pair): ExecutionStatus {
    //        return blockingStub.stopRecordingOrders(pair)
    //    }
}