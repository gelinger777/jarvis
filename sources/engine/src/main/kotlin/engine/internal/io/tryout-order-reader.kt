package engine.internal.io

import common.global.compact
import engine.readers.OrderStreamReader
import eventstore.tools.StreamReader
import net.openhft.chronicle.queue.RollCycles
import util.app

/**
 * Reads recorded raw order stream using OrderStreamReader.
 */
internal fun main(args: Array<String>) {

    val rawStreamReader = StreamReader(
            path = "/Users/vach/workspace/jarvis/dist/data/bitfinex/btc-usd/orders/",
            rollCycle = RollCycles.MINUTELY
    )

    OrderStreamReader(rawStreamReader).stream().forEach { app.log.info(it.compact()) }
}