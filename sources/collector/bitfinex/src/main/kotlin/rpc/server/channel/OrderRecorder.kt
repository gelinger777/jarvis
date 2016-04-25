package rpc.server.channel

import eventstore.storage
import io.grpc.stub.StreamObserver
import proto.Order

internal data class OrderRecorder(val path: String) : StreamObserver<Order> {
    val stream = storage.eventStream(path)

    override fun onNext(order: Order) {
        stream.write(order.toByteArray())
    }

    override fun onError(error: Throwable) {

    }

    override fun onCompleted() {
    }

}