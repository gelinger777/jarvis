package eventstore.client

import com.google.protobuf.ByteString
import common.global.toByteString
import eventstore.client.internal.readReq
import eventstore.client.internal.streamReq
import eventstore.client.internal.writeReq
import proto.eventstore.ProtoES.DataResp
import proto.eventstore.ProtoES.Event
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
                        // execute a write
                        val response = client.blockStub.write(writeReq(path, batch))

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
                readReq(path, start, end),
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
                streamReq(path),
                observer.asGrpcObserver()
        )

        return observer
                .map { it.eventsList }
                .doOnNext { log.debug("observing batch of size : ${it.size}") }
                .unpack()
    }

}
