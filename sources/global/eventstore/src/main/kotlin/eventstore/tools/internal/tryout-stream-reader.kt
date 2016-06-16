package eventstore.tools.internal

import eventstore.tools.io.EventStreamReader
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.RollCycles.HOURLY
import util.app
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal fun main(args: Array<String>) {

    val reader = EventStreamReader("/Users/vach/workspace/jarvis/dist/data/uuid", HOURLY)

    reader.read()
            .map { String(it.second, Charsets.UTF_8) }
            .subscribe { app.log.info(it) }


}

private fun current(): String {
    return DateTimeFormatter.ofPattern("YYY-MM-dd HH:mm:ss").format(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
}