package eventstore.server

import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import proto.eventstore.*
import util.global.*

internal object EventStore : EventStoreGrpc.EventStore {
    val log by logger("event-store-server")

    val conf = readConfiguration("eventStoreConfig");
    val streams = mutableMapOf<String, EventStream>()

    override fun info(request: InfoReq, responseObserver: StreamObserver<InfoResp>) {
        throw UnsupportedOperationException() // todo
    }

    override fun read(request: ReadReq, observer: StreamObserver<ReadResp>) {
        try {
            // get the corresponding event stream
            val es = request.path.getEventStream()

            // create requested data stream
            val source = es.observe(request.start, request.end, request.keepStreaming)
                    .map { it.toByteString() }
                    .batch()
                    .map { it.toReadResponse() }
                    .doOnNext { log.debug("sending batch of size ${it.dataCount}")}

            // subscribe to the stream
            observer.subscribe(source)
        } catch(error: Throwable) {
            observer.onError(error)
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

    private fun readConfiguration(propertyName: String): EventStoreConfig {
        log.info("reading the configuration")
        return EventStoreConfig.newBuilder().readFromFS(propertyName).build();
    }

    private fun toAbsolutePath(relative: String): String {
        return conf.path + relative
    }

    @Synchronized fun String.getEventStream(): EventStream {
        condition(notNullOrEmpty(this))
        return streams.computeIfAbsent(this, { EventStream(it, toAbsolutePath(it)) })
    }

    fun ByteArray.toByteString(): ByteString {
        return ByteString.copyFrom(this)
    }

    fun Collection<ByteString>.toReadResponse(): ReadResp {
        return ReadResp.newBuilder()
                .addAllData(this)
                .build()
    }

}
