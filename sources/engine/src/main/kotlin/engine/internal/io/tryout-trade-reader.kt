package engine.internal.io

import common.global.compact
import engine.readers.TradeStreamReader
import eventstore.tools.io.EventStreamReader
import net.openhft.chronicle.queue.RollCycles
import util.app.log

/**
 * Reads recorded raw order stream using OrderStreamReader.
 */
internal fun main(args: Array<String>) {

    val rawStreamReader = EventStreamReader(
            path = "/Users/vach/workspace/jarvis/dist/data/bitfinex/btc-usd/trades/",
            rollCycle = RollCycles.MINUTELY
    )

    TradeStreamReader(rawStreamReader).stream().forEach { log.info { it.compact() } }
}