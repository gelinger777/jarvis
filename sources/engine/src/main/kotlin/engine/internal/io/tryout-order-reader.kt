package engine.internal.io

import common.global.compact
import engine.readers.OrderStreamReader
import eventstore.tools.io.EventStreamReader
import net.openhft.chronicle.queue.RollCycles
import util.app

/**
 * Reads recorded raw order stream using OrderStreamReader.
 */
internal fun main(args: Array<String>) {

    val rawStreamReader = EventStreamReader(
            path = "/Users/vach/workspace/jarvis/dist/data/bitfinex/btc-usd/orders/",
            rollCycle = RollCycles.MINUTELY
    )

    OrderStreamReader(rawStreamReader).stream().forEach { app.log.info(it.compact()) }
}