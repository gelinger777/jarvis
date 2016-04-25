package eventstore

import com.tars.util.validation.Validator.condition
import global.logger
import rx.Observable
import util.cpu
import util.exceptionUtils.report

class EventStream(val path: String) {
    private val log by logger()
    private val chronicle = storage.chronicle(path)
    private val appender = chronicle.createAppender()

    // local data

    fun lastIndex(): Long {
        return chronicle.lastIndex()
    }

    /**
     * Append a new event to the stream.
     */
    fun write(event: ByteArray): Long {
        synchronized(appender, { return writeFrame(appender, event) })
    }

    /**
     * Stream persisted data.
     */
    fun stream(start: Long = -1, end: Long = -1): Observable<ByteArray> {
        return observe(start, end)
    }

    // realtime data

    /**
     * Stream only realtime.
     */
    fun streamRealtime(): Observable<ByteArray> {
        return storage.watcher(path).stream()
    }

    /**
     * Stream persisted then realtime.
     */
    fun streamRealtime(start: Long = -1): Observable<ByteArray> {
        return observe(start, attachRealtime = true)
    }

    // todo support metadata

    private fun observe(start: Long = -1, end: Long = -1, attachRealtime: Boolean = false): Observable<ByteArray> {
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
                        subscriber.onNext(readFrame(tailer))
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
                else {
                    log.debug("streaming realtime")
                    val watcher = storage.watcher(path)

                    /**
                     * We lock this watcher (which will block watcher-event-loop when it calls checkAndEmit on this watcher,
                     * we synchronize our emissions to the point when we are subscribed to realtime stream, only after that
                     * we let the lock go. This is necessary to guarantee we don't loose any items between streaming local
                     * data and subscribing to realtime stream.
                     */
                    synchronized(watcher, {
                        // make sure not to loose any elements before subscribing
                        while (tailer.nextIndex()) {
                            subscriber.onNext(readFrame(tailer))
                        }

                        // make sure we are on the same index (synced state with watcher)
                        condition(tailer.index() == chronicle.lastIndex())

                        // further subscribe to watcher
                        watcher.stream().subscribe(subscriber)
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