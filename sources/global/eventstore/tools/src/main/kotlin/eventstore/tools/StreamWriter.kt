package eventstore.tools

import eventstore.tools.internal.queue
import net.openhft.chronicle.queue.RollCycles
import util.global.logger

/**
 * Provides api to write data to an event stream with rollup intervals.
 */
class StreamWriter(val path: String, val rollupCycle: RollCycles) {
    private val log = logger("StreamWriter")
    private val chronicle = queue(path, rollupCycle)
    private val appender = chronicle.createAppender()

    var totalBytes = 0
    var totalEvents = 0

    @Synchronized fun write(data: ByteArray) {
        log.debug("${data.size} bytes to $path")
        appender.write(data)
        totalBytes+= data.size
        totalEvents++
    }
}