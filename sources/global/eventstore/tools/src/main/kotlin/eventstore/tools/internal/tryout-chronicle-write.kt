package eventstore.tools.internal

import net.openhft.chronicle.queue.RollCycles
import util.cpu
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal fun main(args: Array<String>) {
    val ch = queue("/Users/vach/workspace/jarvis/dist/data/temp", RollCycles.MINUTELY)

    val ap = ch.createAppender()

    while (true) {

        val current = current()

        println("writing : $current")
        ap.writeText(current)
        cpu.sleep(1000)
    }

}

private fun current(): String {
    return DateTimeFormatter.ofPattern("YYY-MM-dd HH:mm:ss").format(ZonedDateTime.ofInstant (Instant.now(), ZoneOffset.UTC))
}
