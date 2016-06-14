package collector.common

import collector.common.internal.ordersDataPathFor
import collector.common.internal.tradeDataPathFor
import common.IExchange
import eventstore.tools.StreamWriter
import io.grpc.stub.StreamObserver
import proto.common.*
import util.global.complete
import util.global.computeIfAbsent
import util.global.logger

class CollectorService(val client: IExchange) : CollectorGrpc.Collector {
    val log = logger("collector.${client.name().toLowerCase()}")
    val recorders = mutableMapOf<String, StreamWriter>()

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
            log.info { "recording trades to $path" }
            StreamWriter(path)
                    .apply {
                        client.market(request.pair).trades()
                                .map { it.toByteArray() }
                                .forEach { this.write(it) }
                    }
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
            log.info { "recording orders $path" }
            StreamWriter(path)
                    .apply {
                        client.market(request.pair).orders()
                                .map { it.toByteArray() }
                                .forEach { this.write(it) }
                    }
        })

        observer.complete(
                RecordOrdersResp.newBuilder()
                        .setSuccess(true)
                        .build()
        )
    }
}

