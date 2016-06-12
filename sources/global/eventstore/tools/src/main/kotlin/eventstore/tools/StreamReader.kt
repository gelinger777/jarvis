package eventstore.tools

import eventstore.tools.internal.queue
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.RollCycles
import rx.Observable
import util.global.isSubscribed
import util.global.logger
import util.global.size

/**
 * Provides streaming api for reading raw data stream. Make sure to pass the correct roll cycle,
 * it cannot detect it automatically...
 */
class StreamReader(val path: String, val rollCycle: RollCycles = RollCycles.DAILY) {
    private val log = logger("StreamReader")
    private val chronicle = queue(path, rollCycle)
    
    fun read(): Observable<Pair<Long, ByteArray>> {

        // check if observable has data
        if (chronicle.lastIndex() < 0) {
            log.debug { "no data found at $path" }
            return Observable.empty()
        }

        return Observable.create<Pair<Long, ByteArray>> { subscriber ->
            val tailer = chronicle.createTailer()
            val buffer = Bytes.allocateElasticDirect()

            while (subscriber.isSubscribed()) {

                val index = tailer.index()

                if (!tailer.readBytes(buffer)) {
                    break;
                }

                val data = buffer.toByteArray()
                buffer.clear()

                subscriber.onNext(index to data)
                log.debug { "have read ${size(data.size.toLong())} from $path" }
            }

            subscriber.onCompleted()
        }

    }

}