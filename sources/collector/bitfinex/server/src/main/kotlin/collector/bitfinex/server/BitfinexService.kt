package collector.bitfinex.server

import bitfinex.Bitfinex
import collector.bitfinex.server.recorder.RecordingObserver
import eventstore.storage
import util.global.computeIfAbsent
import util.global.logger
import util.global.subscribe
import io.grpc.stub.StreamObserver
import proto.common.*
import common.util.asFolderName
import common.util.respondCollStatus
import common.util.respondRecordOrders
import common.util.respondRecordTrades
import java.io.File

internal class BitfinexService(val bitfinex : Bitfinex) : CollectorGrpc.Collector {
    val log by logger("bitfinex-collector-server")

    val recorders = mutableMapOf<String, Any>()


    override fun status(request: CollStatusReq, observer: StreamObserver<CollStatusResp>) {
        log.debug("getting accessible market pairs")

        val supportedPairs = bitfinex.symbols()

        respondCollStatus(observer, supportedPairs)
    }

    override fun streamTrades(request: StreamTradesReq, observer: StreamObserver<Trade>) {
        observer.subscribe(bitfinex.streamTrades(request.pair))
    }

    override fun streamOrders(request: StreamOrdersReq, observer: StreamObserver<Order>) {
        observer.subscribe(bitfinex.streamOrders(request.pair))
    }

    override fun recordTrades(request: RecordTradesReq, observer: StreamObserver<RecordTradesResp>) {
        val path = tradeDataPath(request.pair)

        recorders.computeIfAbsent(path, {
            val recordingObserver = RecordingObserver<Trade>(it)
            recordingObserver.subscribe(bitfinex.streamTrades(request.pair))
            recordingObserver
        })

        respondRecordTrades(observer, success = true)
    }

    override fun recordOrders(request: RecordOrdersReq, observer: StreamObserver<RecordOrdersResp>) {

        val path = tradeDataPath(request.pair)

        recorders.computeIfAbsent(path, {
            val recordingObserver = RecordingObserver<Order>(it)
            recordingObserver.subscribe(bitfinex.streamOrders(request.pair))
            recordingObserver
        })

        respondRecordOrders(observer, success = true)
    }


    override fun streamHistoricalTrades(request: StreamHistoricalTradesReq, observer: StreamObserver<Trade>) {
        val path = tradeDataPath(request.pair)
        val stream = storage.eventStream(path)

        val dataStream = stream
                .stream(request.startIndex, request.endIndex)
                .map { Trade.parseFrom(it) } // note : unnecessary serialization

        observer.subscribe(dataStream)
    }

    override fun streamHistoricalOrders(request: StreamHistoricalOrdersReq, observer: StreamObserver<Order>) {
        val path = ordersDataPath(request.pair)
        val stream = storage.eventStream(path)

        val dataStream = stream
                .stream(request.startIndex, request.endIndex)
                .map { Order.parseFrom(it) } // note : unnecessary serialization

        observer.subscribe(dataStream)
    }


    private fun tradeDataPath(pair: Pair): String {
        // btc-usd\trades\data.*
        return pair.asFolderName() + File.separator + "trades" + File.separator + "data"
    }

    private fun ordersDataPath(pair: Pair): String {
        // btc-usd\book\data.*
        return pair.asFolderName() + File.separator + "orders" + File.separator + "data"
    }
}

