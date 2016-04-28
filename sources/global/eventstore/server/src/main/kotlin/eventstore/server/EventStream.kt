package eventstore.server

import com.tars.util.validation.Validator.condition
import net.openhft.chronicle.ChronicleQueueBuilder
import rx.Observable
import rx.subjects.PublishSubject
import util.cpu
import util.exceptionUtils.report
import util.global.logger

internal class EventStream(val path: String) {
    private val log by logger()
    private val chronicle = ChronicleQueueBuilder.indexed(path).small().build()
    private val appender = chronicle.createAppender()

    private val writeSubject = PublishSubject.create<ByteArray>()
    private val writeStream = writeSubject.observeOn(cpu.schedulers.io)

    // local data

    fun lastIndex(): Long {
        return chronicle.lastIndex()
    }

    /**
     * Append a new event to the stream.
     */
    fun write(event: ByteArray): Long {
        // info : memory footprint can be optimized if we don't create byte[] and use buffers
        synchronized(appender, {
            // write the event and get the index
            val index = writeByteArrayFrame(appender, event)

            // async notify listeners about this write (use io pool)
            writeSubject.onNext(event)

            return index
        })
    }

    /**
     * Stream persisted data.
     */
    fun stream(start: Long = -1, end: Long = -1, keepStreaming: Boolean = false): Observable<ByteArray> {
        return observe(start, end, keepStreaming)
    }

    // todo support metadata

    private fun observe(start: Long, end: Long, keepStreaming: Boolean): Observable<ByteArray> {
        // let the mess start!
        return Observable.create<ByteArray> { subscriber ->
            var tailer = chronicle.createTailer()

            try {
                val hasStart = start != -1L
                val hasEnd = end != -1L

                log.debug("streaming existing data")

                if (hasStart) {
                    condition(start >= 0)
                    tailer.index(start - 1)
                }

                if (hasEnd) {
                    condition(start < end && end < chronicle.lastIndex())
                }

                // while subscriber is subscribed keep streaming
                while (!subscriber.isUnsubscribed) {

                    if (tailer.nextIndex()) {
                        // if there was upper limit and limit is reached finish
                        if (hasEnd && tailer.index() > end) {
                            break
                        }
                        subscriber.onNext(readFrameAsByteArray(tailer))
                        Thread.`yield`()
                    } else {
                        break
                    }
                }

                // if has end we complete
                if (hasEnd && !subscriber.isUnsubscribed) {
                    subscriber.onCompleted()
                }

                // subscribe to realtime stream and let it complete
                else if (keepStreaming && !subscriber.isUnsubscribed) {
                    log.debug("streaming realtime")

                    synchronized(appender, {
                        // make sure not to loose any messages before subscribing
                        while (tailer.nextIndex()) {
                            subscriber.onNext(readFrameAsByteArray(tailer))
                        }

                        // make sure we are on the same index (synced state with watcher)
                        condition(tailer.index() == chronicle.lastIndex())

                        // further subscribe to watcher
                        writeStream.subscribe(subscriber)
                    })
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
                .subscribeOn(cpu.schedulers.io)
                .observeOn(cpu.schedulers.io)
                .unsubscribeOn(cpu.schedulers.io)
    }

}