package rpc.server

import extensions.logger
import io.grpc.stub.StreamObserver
import org.apache.http.client.methods.RequestBuilder.get
import proto.*
import util.asPair
import util.net.http

internal class CollectorServer : CollectorGrpc.Collector {
    val log by logger()

    override fun accessibleMarketPairs(request: Empty, observer: StreamObserver<Pairs>) {

        http.getString(get("https://api.bitfinex.com/v1/symbols"))
                .map { response ->
                    response.replace(Regex("\\[|\\]|\\n|\""), "")
                            .split(",")
                            .asSequence()
                            .map { str -> str.asPair() }
                            .toList()
                }
                .map { Pairs.newBuilder().addAllPairs(it).build() }
                .ifPresent {
                    observer.onNext(it)
                    observer.onCompleted()
                }
    }

    override fun getCurrentlyRecordingTradePairs(request: Empty, observer: StreamObserver<Pairs>) {
        throw UnsupportedOperationException()
    }

    override fun getCurrentlyRecordingOrderPairs(request: Empty, observer: StreamObserver<Pairs>) {
        throw UnsupportedOperationException()
    }

    override fun shutDown(request: Empty, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun streamTrades(request: Pair, observer: StreamObserver<Trade>) {
        throw UnsupportedOperationException()
    }

    override fun streamOrders(request: Pair, observer: StreamObserver<Order>) {
        throw UnsupportedOperationException()
    }

    override fun getTradeStreamInfo(request: Pair, observer: StreamObserver<TradeStreamInfo>) {
        throw UnsupportedOperationException()
    }

    override fun startRecordingTrades(request: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun stopRecordingTrades(request: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun getOrderStreamInfo(request: Pair, observer: StreamObserver<OrderStreamInfo>) {
        throw UnsupportedOperationException()
    }

    override fun startRecordingOrders(request: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun stopRecordingOrders(request: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun exposeHistoricalTradesData(request: Pair, observer: StreamObserver<InetAddress>) {
        throw UnsupportedOperationException()
    }

    override fun closeHistoricalTradeData(request: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun exposeHistoricalBookData(request: Pair, observer: StreamObserver<InetAddress>) {
        throw UnsupportedOperationException()
    }

    override fun closeHistoricalBookData(request: Pair, observer: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

}
