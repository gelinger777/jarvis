package eventstore.server

import net.openhft.chronicle.ChronicleQueueBuilder
import net.openhft.chronicle.ExcerptAppender
import net.openhft.chronicle.ExcerptTailer
import rx.Observable
import rx.subjects.PublishSubject
import util.cpu
import util.global.isSubscribed
import util.global.logger

internal class EventStream(val name: String, val path: String) {
    private val log by logger("server-event-stream")
    private val chronicle = ChronicleQueueBuilder.indexed(path).small().build()
    private val appender = chronicle.createAppender()

    private val writeSubject = PublishSubject.create<Pair<Long, ByteArray>>()
    private val writeStream = writeSubject.observeOn(cpu.schedulers.io)

    /**
     * Append a new event to the stream.
     */
    fun write(bytes: ByteArray): Long {
        // info : memory footprint can be optimized if we don't create byte[] and use buffers
        log.debug("writing ${bytes.size} bytes to $name")
        synchronized(appender, {
            // write bytes and get the index
            val index = appender.writeFrame(bytes)

            // async notify observers about this write
            if (writeSubject.hasObservers()) {
                writeSubject.onNext(index to bytes)
            }

            return index
        })
    }

    fun writeBatch(events: List<ByteArray>) {
        // info : memory footprint can be optimized if we don't create byte[] and use buffers
        log.debug("$name : writing batch of size ${events.size}")
        synchronized(appender, {

            events.forEach {
                // write the event and get the index
                val index = appender.writeFrame(it)

                // async notify observers about this write
                if (writeSubject.hasObservers()) {
                    writeSubject.onNext(index to it)
                }
            }

        })
    }

    fun observe(start: Long = -1L, end: Long = -1L): Observable<Pair<Long, ByteArray>> {
        return Observable.create<Pair<Long, ByteArray>> { subscriber ->
            var tailer = chronicle.createTailer()

            if (start != -1L) {
                tailer.index(start)
            }

            if (end != -1L) {
                while (subscriber.isSubscribed()) {
                    // if there is data
                    if (tailer.nextIndex()) {
                        // if end was specified and reached finish
                        if (tailer.index() > end) {
                            break
                        }
                        subscriber.onNext(tailer.readFrame())
                    } else {
                        break
                    }
                }
            } else {
                while (subscriber.isSubscribed()) {
                    if (tailer.nextIndex()) {
                        subscriber.onNext(tailer.readFrame())
                    }
                }
            }

            subscriber.onCompleted()
        }
    }

    fun realtime(): Observable<Pair<Long, ByteArray>> {
        return writeStream
    }

    internal fun ExcerptTailer.readFrame(): Pair<Long, ByteArray> {
        val index = this.index();
        // read next message size
        val result = ByteArray(this.readInt())
        // read message content
        this.read(result)
        this.finish()

        return index to result
    }

    internal fun ExcerptAppender.writeFrame(data: ByteArray): Long {
        // current index we write to
        val index = this.index()
        // calculate total message size
        val msgSize = 4 + data.size
        this.startExcerpt(msgSize.toLong())
        // write length of the message
        this.writeInt(data.size)
        // write message content
        this.write(data)
        this.finish()

        return index
    }
}


