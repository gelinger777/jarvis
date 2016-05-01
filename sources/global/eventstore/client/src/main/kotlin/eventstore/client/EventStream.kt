package eventstore.client

import com.google.protobuf.ByteString
import proto.eventstore.ReadReq
import proto.eventstore.ReadResp
import proto.eventstore.WriteReq
import rx.Observable
import rx.Observer
import rx.subjects.PublishSubject
import util.global.*

class EventStream(val path: String, val client: EventStoreClient) {

    val log by logger("client-event-stream")
    val writeStream = PublishSubject.create<ByteArray>()

    init {
        writeStream
                .map { it.toByteString() }
                .batch()
                .subscribe(object : Observer<Collection<ByteString>> {
                    override fun onNext(batch: Collection<ByteString>) {
                        // compose write request with batch of data
                        val writeRequest = WriteReq.newBuilder()
                                .setPath(path)
                                .addAllData(batch)
                                .build()

                        // execute a write
                        val response = client.blockStub.write(writeRequest)

                        if (response.success) {
                            log.debug("successfully wrote batch of size ${batch.size}")
                        } else {
                            report("failed write to eventstore")
                        }

                    }

                    override fun onError(error: Throwable) {
                        throw UnsupportedOperationException()
                    }

                    override fun onCompleted() {
                        throw UnsupportedOperationException()
                    }
                })
    }

    fun write(data: ByteArray) {
        writeStream.onNext(data)
    }

    /**
     * Return batched data stream.
     */
    fun observe(start: Long = -1, end: Long = -1, realtime: Boolean = false): Observable<ByteArray> {
        val observer = PublishSubject.create<ReadResp>()

        client.asyncStub.read(
                ReadReq.newBuilder()
                        .setPath(path)
                        .setStart(start)
                        .setEnd(end)
                        .setKeepStreaming(realtime)
                        .build(),
                observer.asGrpcObserver()
        )

        return observer
                .map { it.dataList }
                .unpack()
                .map { it.toByteArray() }
    }

    // stuff

    fun ByteArray.toByteString(): ByteString {
        return ByteString.copyFrom(this)
    }

}
