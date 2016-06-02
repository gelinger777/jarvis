package collector.common.server

import collector.common.internal.ordersDataPathFor
import collector.common.internal.tradeDataPathFor
import common.IExchange
import eventstore.client.EventStoreClient
import io.grpc.stub.StreamObserver
import proto.common.*
import util.global.complete
import util.global.computeIfAbsent
import util.global.subscribe

class CollectorService(val client: IExchange, val eventStore: EventStoreClient) : CollectorGrpc.Collector {
    val recorders = mutableMapOf<String, Any>()

    override fun info(request: CollInfoReq, observer: StreamObserver<CollInfoResp>) {
        observer.complete(
                CollInfoResp.newBuilder()
                        .addAllAccessibleMarketPairs(client.pairs())
                        .addAllCurrentStreams(recorders.keys)
                        .build()
        )
    }

    override fun recordTrades(request: RecordTradesReq, observer: StreamObserver<RecordTradesResp>) {
        val path = tradeDataPathFor(request.pair)

        recorders.computeIfAbsent(path, {
            RecordingObserver<Trade>(it, eventStore)
                    .apply { this.subscribe(client.market(request.pair).trades()) }
        })

        observer.complete(
                RecordTradesResp.newBuilder()
                        .setSuccess(true)
                        .build()
        )
    }

    override fun recordOrders(request: RecordOrdersReq, observer: StreamObserver<RecordOrdersResp>) {
        val path = ordersDataPathFor(request.pair)

        recorders.computeIfAbsent(path, {
            RecordingObserver<Order>(it, eventStore)
                    .apply { this.subscribe(client.market(request.pair).orders()) }
        })

        observer.complete(
                RecordOrdersResp.newBuilder()
                        .setSuccess(true)
                        .build()
        )
    }

    override fun streamHistoricalTrades(request: StreamHistoricalTradesReq, observer: StreamObserver<Trade>) {
        val path = tradeDataPathFor(request.pair)

        val stream = eventStore.getStream(path)

        val dataStream = stream
                .read(request.startIndex, request.endIndex)
                .map { Trade.parseFrom(it.data) }

        observer.subscribe(dataStream)
    }

    override fun streamHistoricalOrders(request: StreamHistoricalOrdersReq, observer: StreamObserver<Order>) {
        val path = ordersDataPathFor(request.pair)

        val stream = eventStore.getStream(path)

        val dataStream = stream
                .read(request.startIndex, request.endIndex)
                .map { Order.parseFrom(it.data) }

        observer.subscribe(dataStream)
    }
}

