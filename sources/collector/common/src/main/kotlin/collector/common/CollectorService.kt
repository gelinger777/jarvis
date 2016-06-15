package collector.common

import collector.common.internal.ordersDataPathFor
import collector.common.internal.tradeDataPathFor
import common.IExchange
import common.global.compact
import eventstore.tools.io.ESWriter
import io.grpc.stub.StreamObserver
import proto.common.*
import util.global.complete
import util.global.computeIfAbsent
import util.global.logger
import util.global.report
import util.heartBeat

class CollectorService(val client: IExchange, val storeRoot: String) : CollectorGrpc.Collector {
    val log = logger("collector.${client.name().toLowerCase()}")
    val recorders = mutableMapOf<String, ESWriter>()

    override fun info(request: CollInfoReq, observer: StreamObserver<CollInfoResp>) {
        observer.complete(
                CollInfoResp.newBuilder()
                        .addAllAccessibleMarketPairs(client.pairs())
                        .addAllCurrentStreams(recorders.keys)
                        .build()
        )
    }

    override fun recordTrades(request: RecordTradesReq, observer: StreamObserver<RecordTradesResp>) {
        val path = "$storeRoot/${tradeDataPathFor(request.pair)}"

        recorders.computeIfAbsent(path, {
            log.info { "recording trades to $path" }
            ESWriter(path)
                    .apply {
                        val heartbeatKey = "${client.name()}|${request.pair.compact()}"

                        heartBeat.start(heartbeatKey, 10 * 1000, { report("no events for a while") })

                        client.market(request.pair).trades()
                                .map { it.toByteArray() }
                                .forEach {
                                    this.write(it)
                                    heartBeat.beat(heartbeatKey)
                                }
                    }
        })

        observer.complete(
                RecordTradesResp.newBuilder()
                        .setSuccess(true)
                        .build()
        )
    }

    override fun recordOrders(request: RecordOrdersReq, observer: StreamObserver<RecordOrdersResp>) {
        val path = "$storeRoot/${ordersDataPathFor(request.pair)}"

        recorders.computeIfAbsent(path, {
            log.info { "recording orders $path" }
            ESWriter(path)
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

