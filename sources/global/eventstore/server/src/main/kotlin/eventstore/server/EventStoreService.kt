package eventstore.server

import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import proto.eventstore.*
import util.MutableOption
import util.global.condition
import util.global.notNullOrEmpty
import util.global.subscribe

class EventStoreService(val rootPath: String) : EventStoreGrpc.EventStore {


    override fun info(request: InfoReq, observer: StreamObserver<InfoResp>) {
        throw UnsupportedOperationException()
    }

    override fun read(request: ReadReq, observer: StreamObserver<ReadResp>) {
        // get the corresponding event stream
        val es = storage.eventStream(request.path)

        // create requested data stream
        val source = es.stream(request.start, request.end, request.keepStreaming).map { it.toReadResponse() }

        // subscribe to the stream
        observer.subscribe(source)
    }

    override fun write(observer: StreamObserver<WriteResp>): StreamObserver<WriteReq> {
        val writeRequestObserver = object : StreamObserver<WriteReq> {
            val eventStream = MutableOption.empty<EventStream>()

            override fun onNext(next: WriteReq) {
                eventStream
                        .ifNotPresentCompute {
                            condition(notNullOrEmpty(next.path), "first request must contain path of the stream")
                            storage.eventStream(next.path)
                        }
                        .ifPresent { it.write(next.data.toByteArray()) }
            }

            override fun onError(error: Throwable) {
            }

            override fun onCompleted() {
            }
        }


        throw UnsupportedOperationException()
    }


    // extensions

    fun ByteArray.toReadResponse(): ReadResp {
        return ReadResp.newBuilder().setData(ByteString.copyFrom(this)).build()
    }
}
