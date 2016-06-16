package eventstore.tools.internal

import net.openhft.chronicle.queue.RollCycles
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal fun main(args: Array<String>) {
    val ch = queue("/Users/vach/workspace/jarvis/dist/data/test-cycles", RollCycles.MINUTELY)

    val ap = ch.createAppender()

    val time = 42
    val price = 42.4242F
    val vol = 42.4242F

    while (true) {

    }
}

private fun current(): String {
    return DateTimeFormatter.ofPattern("YYY-MM-dd HH:mm:ss").format(ZonedDateTime.ofInstant (Instant.now(), ZoneOffset.UTC))
}
