package rpc.client

import common.GenericCollector
import extensions.grpcDelegate
import io.grpc.ManagedChannelBuilder
import proto.*
import rx.Observable
import rx.subjects.PublishSubject
import util.cpu
import util.empty

class BitfinexClient(val host: String, val port: Int) : GenericCollector {
    var channel = ManagedChannelBuilder
            .forAddress(host, port)
            .executor(cpu.executors.io)
            .build()
    var asyncStub = CollectorGrpc.newStub(channel)
    var blockingStub = CollectorGrpc.newBlockingStub(channel)


    override fun status(): ServiceStatus {
        return blockingStub.status(empty())
    }

    override fun heartBeat(): Observable<ServiceStatus> {
        val subject = PublishSubject.create<ServiceStatus>()
        asyncStub.heartBeat(empty(), subject.grpcDelegate())
        return subject
    }

    override fun accessibleMarketPairs(): List<Pair> {
        return blockingStub.accessibleMarketPairs(empty()).pairsList
    }

    override fun getCurrentlyRecordingTradePairs(): List<Pair> {
        return blockingStub.getCurrentlyRecordingTradePairs(empty()).pairsList
    }

    override fun getCurrentlyRecordingOrderPairs(): List<Pair> {
        return blockingStub.getCurrentlyRecordingOrderPairs(empty()).pairsList
    }

    override fun shutDown(): ExecutionStatus {
        return blockingStub.shutDown(empty())
    }

    override fun streamTrades(pair: Pair): Observable<Trade> {
        val subject = PublishSubject.create<Trade>()
        asyncStub.streamTrades(pair, subject.grpcDelegate())
        return subject
    }

    override fun streamOrders(pair: Pair): Observable<Order> {
        val subject = PublishSubject.create<Order>()
        asyncStub.streamOrders(pair, subject.grpcDelegate())
        return subject
    }

    override fun getTradeStreamInfo(pair: Pair): TradeStreamInfo {
        return blockingStub.getTradeStreamInfo(pair)
    }

    override fun startRecordingTrades(pair: Pair): ExecutionStatus {
        return blockingStub.startRecordingTrades(pair)
    }

    override fun stopRecordingTrades(pair: Pair): ExecutionStatus {
        return blockingStub.stopRecordingTrades(pair)
    }

    override fun getOrderStreamInfo(pair: Pair): OrderStreamInfo {
        return blockingStub.getOrderStreamInfo(pair)
    }

    override fun startRecordingOrders(pair: Pair): ExecutionStatus {
        return blockingStub.startRecordingOrders(pair)

    }

    override fun stopRecordingOrders(pair: Pair): ExecutionStatus {
        return blockingStub.stopRecordingOrders(pair)
    }

    override fun exposeHistoricalTradesData(pair: Pair): InetAddress {
        return blockingStub.exposeHistoricalTradesData(pair)
    }

    override fun closeHistoricalTradeData(pair: Pair): ExecutionStatus {
        return blockingStub.closeHistoricalTradeData(pair)
    }

    override fun exposeHistoricalBookData(pair: Pair): InetAddress {
        return blockingStub.exposeHistoricalBookData(pair)
    }

    override fun closeHistoricalBookData(pair: Pair): ExecutionStatus {
        return blockingStub.closeHistoricalBookData(pair)
    }
}