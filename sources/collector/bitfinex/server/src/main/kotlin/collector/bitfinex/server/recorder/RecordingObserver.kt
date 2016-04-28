package collector.bitfinex.server.recorder

import com.google.protobuf.GeneratedMessage
import io.grpc.stub.StreamObserver
import util.global.wtf

internal class RecordingObserver<T : GeneratedMessage>(val path: String) : StreamObserver<T> {
//    val stream = storage.eventStream(path)

    override fun onNext(value: T) {
//        stream.write(value.toByteArray())
    }

    override fun onError(throwable: Throwable) {
        wtf(throwable)
    }

    override fun onCompleted() {
    }
}
