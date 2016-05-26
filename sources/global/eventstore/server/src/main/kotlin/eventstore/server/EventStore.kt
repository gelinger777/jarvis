package eventstore.server

import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import proto.eventstore.*
import proto.eventstore.ProtoES.*
import util.global.*

internal object EventStore : EventStoreGrpc.EventStore {


    val log by logger("event-store-server")

    val conf = readConfiguration("eventStoreConfig");
    val streams = mutableMapOf<String, EventStream>()

    override fun info(request: InfoReq, responseObserver: StreamObserver<InfoResp>) {
        throw UnsupportedOperationException() // todo
    }

    override fun read(readRequest: ReadReq, observer: StreamObserver<DataResp>) {
        try {
            observer.subscribe(
                    readRequest
                            .validate()
                            .path.getEventStream()
                            .observe(readRequest.start, readRequest.end)
                            .map { it.asEvent() }
                            .batch()
                            .map { it.toDataResponse() }
                            .doOnNext { log.debug("sending batch of size ${it.eventsCount}") }
            )
        } catch(error: Throwable) {
            observer.complete(
                    DataResp.newBuilder()
                            .setSuccess(true)
                            .setError("${error.javaClass.simpleName} : ${error.message}")
                            .build()
            );
        }
    }

    override fun stream(streamRequest: StreamReq, observer: StreamObserver<DataResp>) {
        try {
            observer.subscribe(
                    streamRequest
                            .validate()
                            .path.getEventStream()
                            .realtime()
                            .map { it.asEvent() }
                            .batch()
                            .map { it.toDataResponse() }
                            .doOnNext { log.debug("sending batch of size ${it.eventsCount}") }
            )


        } catch(error: Throwable) {
            observer.complete(
                    DataResp.newBuilder()
                            .setSuccess(false)
                            .setError("${error.javaClass.simpleName} : ${error.message}")
                            .build()
            )
        }
    }

    override fun write(request: WriteReq, responseObserver: StreamObserver<WriteResp>) {
        try {
            // get the corresponding event stream
            val es = request.path.getEventStream()

            // write requested data
            es.writeBatch(request.dataList.map { it.toByteArray() })
        } catch(error: Throwable) {

            // if any error acknowledge a failure
            responseObserver.complete(
                    WriteResp.newBuilder()
                            .setSuccess(false)
                            .setError(error.message)
                            .build()
            )
        }

        // acknowledge successful execution
        responseObserver.complete(
                WriteResp.newBuilder()
                        .setSuccess(true)
                        .build()
        )
    }

    override fun delete(request: DelReq, responseObserver: StreamObserver<DelResp>) {
        throw UnsupportedOperationException() // todo
    }

    // stuff

    private fun ReadReq.validate(): ReadReq {
        condition(notNullOrEmpty(this.path), "path must be provided")

        if (this.start < 0) {
            condition(this.start == -1L)
        }

        if (this.end < 0) {
            condition(this.end == -1L)
        } else {
            condition(this.start < this.end)
        }
        return this;
    }

    private fun StreamReq.validate(): StreamReq {
        condition(notNullOrEmpty(this.path), "path must be provided")
        return this;
    }

    private fun readConfiguration(propertyName: String): EventStoreConfig {
        log.info("reading the configuration")
        return EventStoreConfig.newBuilder().readFromFS(propertyName).build();
    }

    private fun toAbsolutePath(relative: String): String {
        return conf.path + relative
    }

    private @Synchronized fun String.getEventStream(): EventStream {
        condition(notNullOrEmpty(this))
        return streams.computeIfAbsent(this, { EventStream(it, toAbsolutePath(it)) })
    }

    private fun ByteArray.toByteString(): ByteString {
        return ByteString.copyFrom(this)
    }

    private fun Collection<Event>.toDataResponse(): DataResp {
        return DataResp.newBuilder()
                .setSuccess(true)
                .addAllEvents(this)
                .build()
    }

    private fun Pair<Long, ByteArray>.asEvent(): Event {
        return Event.newBuilder()
                .setIndex(this.first)
                .setData(ByteString.copyFrom(this.second))
                .build();
    }

}
