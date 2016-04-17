package eventstore

import global.logger
import writeFrame

class EventStream(val path: String) {
    val log by logger()
    val chronicle = storage.getChronicle(path)
    val appender = chronicle.createAppender()

    fun write(event: ByteArray) {
        writeFrame(appender, event)
    }

    //    fun observe(from: Long, to: Long): Observable<ByteArray> {
    //        return Observable.create<ByteArray> { subscriber ->
    //            var tailer = chronicle.createTailer()
    //            var realtime: Observable<ByteArray>? = null
    //            try {
    //                // prepare realtime stream
    //                if (to == -1L) {
    //                    realtime = Watchers.get(path).stream()
    //                }
    //
    //                // if from is specified set starting index
    //                if (from > 0) {
    //                    tailer!!.index(from - 1)
    //                }
    //
    //                log.debug("streaming from existing data")
    //
    //                // while subscriber is subscribed keep streaming
    //                while (!subscriber.isUnsubscribed) {
    //
    //                    // read next entry
    //                    if (tailer!!.nextIndex()) {
    //                        //publish data
    //                        subscriber.onNext(Chronicles.readFrame(tailer))
    //                    } else {
    //                        break
    //                    }
    //
    //                    // if there was upper limit and limit is reached finish
    //                    if (to != -1 && tailer.index() > to) {
    //                        break
    //                    }
    //                }
    //
    //                // start streaming from realtime stream
    //                if (!subscriber.isUnsubscribed && to == -1) {
    //                    log.debug("streaming from realtime stream")
    //                    realtime!!.subscribe(subscriber)
    //                    return@Observable.create
    //                }
    //
    //                // acknowledge completion
    //                subscriber.onCompleted()
    //            } catch (cause: Throwable) {
    //                // publish error
    //                report(cause)
    //                subscriber.onError(cause)
    //            } finally {
    //                // release allocated resource
    //                if (tailer != null) {
    //                    tailer.close()
    //                    tailer = null
    //                }
    //            }
    //        }
    //    }

    fun close() {
        appender.close()
    }

}