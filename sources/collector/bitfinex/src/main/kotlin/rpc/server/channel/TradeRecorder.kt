package rpc.server.channel

import eventstore.storage
import io.grpc.stub.StreamObserver
import proto.Trade

internal data class TradeRecorder(val path: String) : StreamObserver<Trade> {
    val stream = storage.eventStream(path)

    override fun onNext(trade: Trade) {
        stream.write(trade.toByteArray())
    }

    override fun onError(error: Throwable) {

    }

    override fun onCompleted() {
    }

}