package engine.internal.io

import bitfinex.Bitfinex
import common.global.compact
import common.global.pair
import eventstore.tools.io.EventStreamWriter
import net.openhft.chronicle.queue.RollCycles
import util.app.log

/**
 * Record trades from bitfinex using eventstore tools.
 */
internal fun main(args: Array<String>) {

    val rawStreamWriter = EventStreamWriter(
            path = "/Users/vach/workspace/jarvis/dist/data/bitfinex/btc-usd/trades/",
            cycles = RollCycles.MINUTELY
    )

    Bitfinex().market(pair("btc", "usd"))
            .trades()
            .doOnNext { log.debug { "bitfinex/btc-usd : ${it.compact()}" } }
            .map { it.toByteArray() }
            .forEach { rawStreamWriter.write(it) }

    readLine()
}