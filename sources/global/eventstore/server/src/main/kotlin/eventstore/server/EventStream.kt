package eventstore.server

import eventstore.tools.write
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.ChronicleQueueBuilder
import rx.Observable
import rx.subjects.PublishSubject
import util.app
import util.cpu
import util.global.isSubscribed
import util.global.logger

internal class EventStream(val name: String, val path: String) {
    private val log by lazyLogger("server-event-stream")
    private val chronicle = ChronicleQueueBuilder.single(path).build()
    private val appender = chronicle.createAppender()

    private val writeSubject = PublishSubject.create<Pair<Long, ByteArray>>()
    private val writeStream = writeSubject.observeOn(cpu.schedulers.io)

    // some statistics
    private var totalBytes = 0
    private var totalEvents = 0
    private var created = -1L
    private var lastWrite = 0L

    /**
     * Append a new event to the stream.
     */
    fun write(bytes: ByteArray): Long {
        // info : memory footprint can be optimized if we don't create byte[] and use buffers
        log.debug("writing ${bytes.size} bytes to $name")
        synchronized(appender, {
            // write bytes and get the index
            val index = appender.write(bytes)

            totalBytes += bytes.size
            totalEvents++
            lastWrite = app.time()

            // async notify observers about this write
            log.info("broadcasting")
            writeSubject.onNext(index to bytes)

            return index
        })
    }

    fun writeBatch(events: List<ByteArray>) {
        // info : memory footprint can be optimized if we don't create byte[] and use buffers
        log.debug("$name : writing batch of size ${events.size}")
        synchronized(appender, {

            events.forEach {
                // write the event and get the index
                val index = appender.write(it)

                // async notify observers about this write
                writeSubject.onNext(index to it)
            }

        })
    }

    fun observe(start: Long = -1L, end: Long = -1L): Observable<Pair<Long, ByteArray>> {
        return Observable.create<Pair<Long, ByteArray>> { subscriber ->
            var tailer = chronicle.createTailer()
            val buffer = Bytes.allocateElasticDirect()


            if (start != -1L) {
                tailer.moveToIndex(start)
            }

            if (end != -1L) {

                while (subscriber.isSubscribed() && tailer.readBytes(buffer)) {
                    // if there is data
                    val index = tailer.index()

                    if (index > end) {
                        break
                    }

                    val data = buffer.toByteArray()
                    buffer.clear()

                    subscriber.onNext(index to data)
                }
            } else {
                while (subscriber.isSubscribed() && tailer.readBytes(buffer)) {
                    val index = tailer.index()
                    val data = buffer.toByteArray()
                    buffer.clear()
                    subscriber.onNext(index to data)
                }
            }

            subscriber.onCompleted()
        }
    }

    fun realtime(): Observable<Pair<Long, ByteArray>> {
        return writeStream
    }

}


