package eventstore.tools.io.bytes

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

        // write data
        appender.write(data)
        log.debug(Supplier { "wrote ${data.size} bytes to $path" })

        // update stats
        totalBytes += data.size
        totalEvents++

        // print occasionally
        withProbability(
                1 / 42.0,
                { log.trace("$path - full size : ${size(totalBytes)}, number of events : $totalEvents events, average message size : ${size(totalBytes / totalEvents)}") }
        )
    }

}