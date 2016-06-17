package eventstore.tools.io.bytes

import eventstore.tools.internal.queue
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.RollCycle
import net.openhft.chronicle.queue.RollCycles
import rx.Observable
import util.global.isSubscribed
import util.global.logger
import util.global.size
import util.global.withProbability

/**
 * Provides streaming api for reading raw data stream. Supports unsubscribe.
 */
class BytesReader(val path: String, val rollCycle: RollCycle = RollCycles.DAILY) {
    private val log = logger("EventStreamReader")
    private val chronicle = queue(path, rollCycle)

    fun read(): Observable<Pair<Long, ByteArray>> {

        // check if observable has data
        if (chronicle.lastIndex() < 0) {
            log.debug { "no data found at $path" }
            return Observable.empty()
        }

        return Observable.create<Pair<Long, ByteArray>> { subscriber ->
            chronicle.createTailer().use {

                val buffer = Bytes.allocateElasticDirect()
                var totalBytes = 0L
                var totalEvents = 0L

                while (subscriber.isSubscribed()) {

                    // read data
                    val index = it.index()
                    if (!it.readBytes(buffer)) {
                        break;
                    }
                    val data = buffer.toByteArray()
                    buffer.clear()
                    log.debug { "have read ${size(data.size.toLong())} from $path" }

                    subscriber.onNext(index to data)

                    // update stats
                    totalBytes += data.size
                    totalEvents++

                    // print occasionally
                    withProbability(
                            1 / 42.0,
                            { log.trace("$path - full size : ${size(totalBytes)}, number of events : $totalEvents events, average message size : ${size(totalBytes / totalEvents)}") }
                    )
                }

                subscriber.onCompleted()
            }
        }

    }

}