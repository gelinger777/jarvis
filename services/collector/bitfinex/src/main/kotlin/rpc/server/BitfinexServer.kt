package rpc.server

import extensions.logger
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import proto.*
import util.cpu

class BitfinexServer(port: Int) : CollectorGrpc.Collector {
    val log by logger()
    val server = ServerBuilder
            .forPort(port)
            .addService(CollectorGrpc.bindService(this))
            .executor(cpu.executors.io)
            .build()

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                log.info("shutting down bitfinex server")
                server.shutdown()
            }
        })
    }


    override fun status(request: Empty, responseObserver: StreamObserver<ServiceStatus>) {
        val properties = mutableMapOf<String, String>()
        val build = ServiceStatus.newBuilder()
                .setName("bitfinex")
                .putAllProperties(properties)
                .build()

        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }

    override fun heartBeat(request: Empty, responseObserver: StreamObserver<ServiceStatus>) {
        throw UnsupportedOperationException()
    }

    override fun accessibleMarketPairs(request: Empty, responseObserver: StreamObserver<Pairs>) {
        throw UnsupportedOperationException()
    }

    override fun getCurrentlyRecordingTradePairs(request: Empty, responseObserver: StreamObserver<Pairs>) {
        throw UnsupportedOperationException()
    }

    override fun getCurrentlyRecordingOrderPairs(request: Empty, responseObserver: StreamObserver<Pairs>) {
        throw UnsupportedOperationException()
    }

    override fun shutDown(request: Empty, responseObserver: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun streamTrades(request: Pair, responseObserver: StreamObserver<Trade>) {
        throw UnsupportedOperationException()
    }

    override fun streamOrders(request: Pair, responseObserver: StreamObserver<Order>) {
        throw UnsupportedOperationException()
    }

    override fun getTradeStreamInfo(request: Pair, responseObserver: StreamObserver<TradeStreamInfo>) {
        throw UnsupportedOperationException()
    }

    override fun startRecordingTrades(request: Pair, responseObserver: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun stopRecordingTrades(request: Pair, responseObserver: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun getOrderStreamInfo(request: Pair, responseObserver: StreamObserver<OrderStreamInfo>) {
        throw UnsupportedOperationException()
    }

    override fun startRecordingOrders(request: Pair, responseObserver: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun stopRecordingOrders(request: Pair, responseObserver: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun exposeHistoricalTradesData(request: Pair, responseObserver: StreamObserver<InetAddress>) {
        throw UnsupportedOperationException()
    }

    override fun closeHistoricalTradeData(request: Pair, responseObserver: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

    override fun exposeHistoricalBookData(request: Pair, responseObserver: StreamObserver<InetAddress>) {
        throw UnsupportedOperationException()
    }

    override fun closeHistoricalBookData(request: Pair, responseObserver: StreamObserver<ExecutionStatus>) {
        throw UnsupportedOperationException()
    }

}
