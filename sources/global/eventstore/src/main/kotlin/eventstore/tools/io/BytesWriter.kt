package eventstore.tools.io

import eventstore.tools.internal.queue
import eventstore.tools.internal.write
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.RollCycles.DAILY
import org.apache.logging.log4j.util.Supplier
import util.global.logger
import util.global.size
import util.global.withProbability

/**
 * Provides api to write data to an event stream with rollup intervals.
 * By default rollup interval is daily.
 */
class BytesWriter(val path: String, val cycles: RollCycles = DAILY) {
    private val log = logger("EventStreamWriter")
    private val chronicle = queue(path, cycles)
    private val appender = chronicle.createAppender()

    var totalBytes = 0L
    var totalEvents = 0L

    init {
        log.info { "created event stream writer with ${cycles.name.toLowerCase()} cycles and path '$path'" }
    }

    @Synchronized fun write(data: ByteArray) {
        log.debug(Supplier { "writing ${data.size} bytes to $path" })

        // write the
        appender.write(data)

        // update stats
        totalBytes += data.size
        totalEvents++

        withProbability(
                1 / 10.0,
                { log.trace("$path - full size : ${size(totalBytes)}, number of events : $totalEvents events, average message size : ${size(totalBytes / totalEvents)}") }
        )
    }

}