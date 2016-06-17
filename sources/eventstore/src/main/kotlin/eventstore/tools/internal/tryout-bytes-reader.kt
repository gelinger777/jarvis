package eventstore.tools.internal

import eventstore.tools.io.bytes.BytesReader
import net.openhft.chronicle.queue.RollCycles.HOURLY
import util.app

internal fun main(args: Array<String>) {

    val reader = BytesReader("/Users/vach/workspace/jarvis/dist/data/uuid", HOURLY)

    reader.read()
            .map { String(it.second, Charsets.UTF_8) }
            .subscribe { app.log.info(it) }
}
