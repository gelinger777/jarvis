package collector.common.internal

import collector.common.server.CollectorService
import io.grpc.stub.StreamObserver
import proto.common.CollInfoResp
import proto.common.Pair
import proto.common.RecordOrdersResp
import proto.common.RecordTradesResp
import java.io.File


internal fun respondCollInfo(observer: StreamObserver<CollInfoResp>, accessiblePairs: List<Pair>) {
    observer.onNext(
            CollInfoResp.newBuilder()
                    .addAllAccessibleMarketPairs(accessiblePairs)
                    .build()
    );
    observer.onCompleted()
}

internal fun respondRecordTrades(observer: StreamObserver<RecordTradesResp>, success: Boolean) {
    observer.onNext(
            RecordTradesResp.newBuilder()
                    .setSuccess(success)
                    .build()
    );
    observer.onCompleted()
}

internal fun respondRecordOrders(observer: StreamObserver<RecordOrdersResp>, success: Boolean) {
    observer.onNext(
            RecordOrdersResp.newBuilder()
                    .setSuccess(success)
                    .build()
    );
    observer.onCompleted()
}

private fun Pair.asFolderName(): String {
    // btc-usd
    return "${base.symbol.toLowerCase()}-${quote.symbol.toLowerCase()}"
}

// path

internal fun CollectorService.tradeDataPathFor(pair: Pair): String {
    // exchange/btc-usd/trades
    return client.name().toLowerCase() + File.separator + pair.asFolderName() + File.separator + "trades"
}

internal fun CollectorService.ordersDataPathFor(pair : Pair): String {
    // exchange/btc-usd/orders
    return client.name().toLowerCase() + File.separator + pair.asFolderName() + File.separator + "orders"
}