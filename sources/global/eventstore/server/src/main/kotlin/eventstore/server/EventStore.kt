package eventstore.server

import eventstore.server.internal.asEvent
import eventstore.server.internal.toDataResponse
import eventstore.server.internal.validate
import io.grpc.stub.StreamObserver
import proto.eventstore.EventStoreGrpc
import proto.eventstore.ProtoES.*
import util.global.*

internal class EventStore(val rootPath: String) : EventStoreGrpc.EventStore {

    val log = logger("event-store-server")
    val streams = mutableMapOf<String, EventStream>()

    override fun info(request: InfoReq, responseObserver: StreamObserver<InfoResp>) {
        throw UnsupportedOperationException() // todo
    }

    override fun read(readRequest: ReadReq, observer: StreamObserver<DataResp>) {
        try {
            observer.subscribe(
                    readRequest
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

    private @Synchronized fun String.getEventStream(): EventStream {
        condition(notNullOrEmpty(this))
        return streams.computeIfAbsent(this, { EventStream(it, "$rootPath$it") })
    }

}
