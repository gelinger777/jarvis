package rpc.client

import common.GenericCollector
import global.asGrpcObserver
import global.empty
import io.grpc.ManagedChannelBuilder
import proto.*
import rx.Observable
import rx.subjects.PublishSubject
import util.cpu

class BitfinexClient(val host: String, val port: Int) : GenericCollector {
    private var channel = ManagedChannelBuilder
            .forAddress(host, port)
            .executor(cpu.executors.io)
            .build()
    private var asyncStub = CollectorGrpc.newStub(channel)
    private var blockingStub = CollectorGrpc.newBlockingStub(channel)

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
        asyncStub.streamTrades(pair, subject.asGrpcObserver())
        return subject
    }

    override fun streamOrders(pair: Pair): Observable<Order> {
        val subject = PublishSubject.create<Order>()
        asyncStub.streamOrders(pair, subject.asGrpcObserver())
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