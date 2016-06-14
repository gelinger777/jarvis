package engine.internal.io

import common.global.compact
import engine.readers.TradeStreamReader
import eventstore.tools.StreamReader
import net.openhft.chronicle.queue.RollCycles
import util.app.log

/**
 * Reads recorded raw order stream using OrderStreamReader.
 */
internal fun main(args: Array<String>) {

    val rawStreamReader = StreamReader(
            path = "/Users/vach/workspace/jarvis/dist/data/bitfinex/btc-usd/trades/",
            rollCycle = RollCycles.MINUTELY
    )

    TradeStreamReader(rawStreamReader).stream().forEach { log.info { it.compact() } }
}