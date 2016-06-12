package eventstore.tools

import eventstore.tools.internal.queue
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.RollCycles.DAILY
import org.apache.logging.log4j.util.Supplier
import util.app
import util.global.logger
import util.global.size

/**
 * Provides api to write data to an event stream with rollup intervals.
 * By default rollup interval is daily
 */
class StreamWriter(val path: String, val rollCycle: RollCycles = DAILY) {
    private val log = logger("StreamWriter")
    private val chronicle = queue(path, rollCycle)
    private val appender = chronicle.createAppender()

    var totalBytes = 0L
    var totalEvents = 0L

    @Synchronized fun write(data: ByteArray) {
        log.debug(Supplier { "writing ${data.size} bytes to $path" })

        // write the
        appender.write(data)

        // update stats
        totalBytes += data.size
        totalEvents++

        // occasionally log the average message size
        if (app.random(9.0 / 10)) {
            log.trace("$path - full size : ${size(totalBytes)}, number of events : $totalEvents events, average message size : ${size(totalBytes / totalEvents)}")
        }
    }

}