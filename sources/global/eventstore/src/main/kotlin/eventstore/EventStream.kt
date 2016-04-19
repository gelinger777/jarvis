package eventstore

import com.tars.util.validation.Validator.condition
import global.logger
import global.whatever
import readFrame
import rx.Observable
import util.cpu
import util.exceptionUtils.report
import writeFrame

class EventStream(val path: String) {
    private val log by logger()
    private val chronicle = storage.chronicle(path)
    private val appender = chronicle.createAppender()

    // local data

    /**
     * Append a new event to the stream.
     */
    fun write(event: ByteArray) {
        synchronized(appender, { writeFrame(appender, event) })
    }

    /**
     * Stream all persisted data.
     */
    fun streamExisting(): Observable<ByteArray> {
        return observe()
    }

    /**
     * Stream portion of persisted data.
     */
    fun streamFromTo(start: Long, end: Long): Observable<ByteArray> {
        return whatever {}
    }


    /**
     * Stream persisted data from specified start.
     */
    fun streamFrom(start: Long): Observable<ByteArray> {
        return whatever {}
    }


    /**
     * Stream persisted data from beginning to specified end.
     */
    fun streamTo(end: Long): Observable<ByteArray> {
        return whatever {}
    }

    // realtime data

    /**
     * Stream only realtime.
     */
    fun streamRealtime(): Observable<ByteArray> {
        return whatever {}
    }

    /**
     * Stream all persisted then realtime.
     */
    fun streamRealtimeAll(): Observable<ByteArray> {
        return whatever {}
    }

    /**
     * Stream persisted data from specified start then realtime.
     */
    fun streamRealtimeFrom(): Observable<ByteArray> {
        return whatever {}
    }


    private fun observe(start: Long = -1, end: Long = -1, attachRealtime: Boolean = false): Observable<ByteArray> {
        // let the mess start!
        return Observable.create<ByteArray> { subscriber ->
            var tailer = chronicle.createTailer()

            try {
                val hasStart = start != -1L
                val hasEnd = end != -1L

                log.debug("streaming existing data")

                if (hasStart) {
                    condition(start >= 0) // todo test
                    tailer.index(start - 1)
                }

                if (hasEnd) {
                    condition(start < end && end < chronicle.lastIndex())
                }

                // while subscriber is subscribed keep streaming
                while (!subscriber.isUnsubscribed) {

                    if (tailer.nextIndex()) {
                        subscriber.onNext(readFrame(tailer))
                    } else {
                        break
                    }

                    // if there was upper limit and limit is reached finish
                    if (hasEnd && tailer.index() > end) {
                        break
                    }
                }

                // start streaming from realtime stream
                if (end == -1L && !subscriber.isUnsubscribed) {
                    log.debug("streaming realtime")
                    val watcher = storage.watcher(path)

                    synchronized(watcher, {
                        // make sure not to loose any elements before subscribing
                        while (tailer.index() < watcher.currentIndex()) {
                            subscriber.onNext(readFrame(tailer))
                        }

                        // further subscribe to watcher
                        watcher.stream().subscribe(subscriber)
                    })
                } else {
                    // acknowledge completion
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
                .subscribeOn(cpu.schedulers.io)
                .observeOn(cpu.schedulers.io)
                .unsubscribeOn(cpu.schedulers.io)
    }

}