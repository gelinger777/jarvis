package collector.bitfinex.server

import bitfinex.Bitfinex
import collector.bitfinex.server.recorder.RecordingObserver
import common.global.*
import eventstore.client.EventStoreClient
import io.grpc.stub.StreamObserver
import proto.bitfinex.ProtoBitfinex.BitfinexCollectorConfig
import proto.common.*
import util.global.computeIfAbsent
import util.global.logger
import util.global.subscribe
import java.io.File

internal class BitfinexCollectorService(val config: BitfinexCollectorConfig, val bitfinex: Bitfinex, val eventStore: EventStoreClient) : CollectorGrpc.Collector {
    val log by logger("bitfinex-collector-server")

    val recorders = mutableMapOf<String, Any>()

    init {
        log.debug("initiating collection by config")
        config.tradesList.forEach {
            recordTradesOf(it)
        }
        config.ordersList.forEach {
            recordOrdersOf(it)
        }
    }

    override fun info(request: CollInfoReq, observer: StreamObserver<CollInfoResp>) {
        log.debug("getting accessible market pairs")

        val supportedPairs = bitfinex.pairs()

        respondCollInfo(observer, supportedPairs)
    }

    override fun streamTrades(request: StreamTradesReq, observer: StreamObserver<Trade>) {
        observer.subscribe(bitfinex.market(request.pair).trades())
    }

    override fun streamOrders(request: StreamOrdersReq, observer: StreamObserver<Order>) {
        observer.subscribe(bitfinex.market(request.pair).orders())
    }

    override fun recordTrades(request: RecordTradesReq, observer: StreamObserver<RecordTradesResp>) {
        recordTradesOf(request.pair)
        respondRecordTrades(observer, success = true)
    }

    override fun recordOrders(request: RecordOrdersReq, observer: StreamObserver<RecordOrdersResp>) {
        recordOrdersOf(request.pair)
        respondRecordOrders(observer, success = true)
    }


    override fun streamHistoricalTrades(request: StreamHistoricalTradesReq, observer: StreamObserver<Trade>) {
        val path = request.pair.asTradeDataPath()
        val stream = eventStore.getStream(path)

        val dataStream = stream
                .read(request.startIndex, request.endIndex)
                .map { Trade.parseFrom(it.data) }

        observer.subscribe(dataStream)
    }

    override fun streamHistoricalOrders(request: StreamHistoricalOrdersReq, observer: StreamObserver<Order>) {
        val path = request.pair.asOrdersDataPath()
        val stream = eventStore.getStream(path)

        val dataStream = stream
                .read(request.startIndex, request.endIndex)
                .map { Order.parseFrom(it.data) }

        observer.subscribe(dataStream)
    }

    // stuff

    private fun recordTradesOf(pair: Pair) {
        log.info("recording trades of ${pair.json()}")
        val path = pair.asTradeDataPath()

        recorders.computeIfAbsent(path, {
            RecordingObserver<Trade>(it, eventStore).apply { this.subscribe(bitfinex.market(pair).trades()) }
        })
    }

    private fun recordOrdersOf(pair: Pair) {
        log.info("recording orders of ${pair.json()}")
        val path = pair.asOrdersDataPath()

        recorders.computeIfAbsent(path, {
            RecordingObserver<Order>(it, eventStore).apply { this.subscribe(bitfinex.market(pair).orders()) }
        })
    }


    private fun Pair.asTradeDataPath(): String {
        // btc-usd\trades
        return this.asFolderName() + File.separator + "trades"
    }

    private fun Pair.asOrdersDataPath(): String {
        // btc-usd\orders
        return this.asFolderName() + File.separator + "orders"
    }
}

