package eventstore.server

import net.openhft.chronicle.ChronicleQueueBuilder
import net.openhft.chronicle.ExcerptAppender
import net.openhft.chronicle.ExcerptTailer
import rx.Observable
import rx.subjects.PublishSubject
import util.cpu
import util.global.condition
import util.global.logger
import util.global.report
import util.global.wtf

internal class EventStream(val name: String, val path: String) {
    private val log by logger("server-event-stream")
    private val chronicle = ChronicleQueueBuilder.indexed(path).small().build()
    private val appender = chronicle.createAppender()

    private val writeSubject = PublishSubject.create<ByteArray>()
    private val writeStream = writeSubject.observeOn(cpu.schedulers.io)

    /**
     * Append a new event to the stream.
     */
    fun write(event: ByteArray): Long {
        // info : memory footprint can be optimized if we don't create byte[] and use buffers
        log.debug("writing ${event.size} bytes to $name")
        synchronized(appender, {
            // write the event and get the index
            val index = writeByteArrayFrame(appender, event)

            // async notify observers about this write
            if (writeSubject.hasObservers()) {
                writeSubject.onNext(event)
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
                writeByteArrayFrame(appender, it)

                // async notify observers about this write
                if (writeSubject.hasObservers()) {
                    writeSubject.onNext(it)
                }
            }

        })
    }

    fun observe(start: Long, end: Long, realtime: Boolean): Observable<ByteArray> {
        return Observable.create<ByteArray> { subscriber ->
            var tailer = chronicle.createTailer()

            try {
                val startSpecified = start != -1L
                val endSpecified = end != -1L

                val useExisting = startSpecified || !realtime

                if (useExisting) {
                    log.debug("$name : streaming existing data")

                    if (startSpecified) {
                        condition(start >= 0)
                        tailer.index(start - 1)
                    }

                    if (endSpecified) {
                        condition(start < end && end < chronicle.lastIndex())
                    }

                    // while subscriber is subscribed keep streaming
                    while (!subscriber.isUnsubscribed) {

                        // if there is data
                        if (tailer.nextIndex()) {
                            // if end was specified and reached finish
                            if (endSpecified && tailer.index() > end) {
                                break
                            }
                            subscriber.onNext(readFrameAsByteArray(tailer))
                        } else {
                            break
                        }
                    }
                }

                if (realtime) {

                    log.debug("$name : streaming realtime")
                    synchronized(appender, {

                        if (useExisting) {
                            // make sure not to loose any messages before subscribing
                            while (tailer.nextIndex() && !subscriber.isUnsubscribed) {
                                subscriber.onNext(readFrameAsByteArray(tailer))
                            }

                            // make sure we are on the same index (synced state with watcher)
                            condition(tailer.index() == chronicle.lastIndex())
                        }

                        // further subscribe to watcher
                        if (!subscriber.isUnsubscribed) {
                            writeStream.subscribe(subscriber)
                        }
                    })
                } else {
                    subscriber.onCompleted()
                }
            } catch (cause: Throwable) {
                // publish error
                report(cause)
                subscriber.onError(cause)
            } finally {
                // release allocated resource
                tailer.close()
            }
        }
        //                .subscribeOn(cpu.schedulers.io)
        //                .observeOn(cpu.schedulers.io)
        //                .unsubscribeOn(cpu.schedulers.io)
    }

    internal fun readFrameAsByteArray(tailer: ExcerptTailer): ByteArray {
        // read next message size
        val result = ByteArray(tailer.readInt())
        // read message content
        tailer.read(result)
        tailer.finish()
        return result
    }

    internal fun writeByteArrayFrame(appender: ExcerptAppender, data: ByteArray): Long {
        // current index we write to
        val index = appender.index()
        // calculate total message size
        val msgSize = 4 + data.size
        appender.startExcerpt(msgSize.toLong())
        // write length of the message
        appender.writeInt(data.size)
        // write message content
        appender.write(data)
        appender.finish()

        return index
    }
}