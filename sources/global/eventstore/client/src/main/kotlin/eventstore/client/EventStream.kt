package eventstore.client

import com.google.protobuf.ByteString
import proto.eventstore.*
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
                        report(error)
                    }

                    override fun onCompleted() {
                    }
                })
    }

    fun write(data: ByteArray) {
        writeStream.onNext(data)
    }

    fun read(start: Long = -1, end: Long = -1): Observable<Event> {
        val observer = PublishSubject.create<DataResp>()

        client.asyncStub.read(
                ReadReq.newBuilder()
                        .setPath(path)
                        .setStart(start)
                        .setEnd(end)
                        .build(),
                observer.asGrpcObserver()
        )

        return observer
                .map { it.eventsList }
                .doOnNext { log.debug("observing batch of size : ${it.size}") }
                .unpack()
    }

    fun stream(): Observable<Event> {
        val observer = PublishSubject.create<DataResp>()

        client.asyncStub.stream(
                StreamReq.newBuilder()
                        .setPath(path)
                        .build(),
                observer.asGrpcObserver()
        )

        return observer
                .map { it.eventsList }
                .doOnNext { log.debug("observing batch of size : ${it.size}") }
                .unpack()
    }

    // stuff

    fun ByteArray.toByteString(): ByteString {
        return ByteString.copyFrom(this)
    }

}
