package eventstore.tools.internal

import eventstore.tools.io.ESWriter
import net.openhft.chronicle.queue.RollCycles
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal fun main(args: Array<String>) {

    val writer = ESWriter("/Users/vach/workspace/jarvis/dist/data/uuid", RollCycles.MINUTELY)

    while (true) {
        writer.write(current())
        Thread.sleep(50)
    }

}

private fun current(): String {
    return DateTimeFormatter.ofPattern("YYY-MM-dd HH:mm:ss").format(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
}