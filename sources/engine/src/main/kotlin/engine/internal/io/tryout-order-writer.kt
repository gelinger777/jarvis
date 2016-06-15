package engine.internal.io

import bitfinex.Bitfinex
import common.global.compact
import common.global.pair
import eventstore.tools.io.ESWriter
import net.openhft.chronicle.queue.RollCycles
import util.app.log

/**
 * Record orders from bitfinex using eventstore tools.
 */
internal fun main(args: Array<String>) {

    val rawStreamWriter = ESWriter(
            path = "/Users/vach/workspace/jarvis/dist/data/bitfinex/btc-usd/orders/",
            rollCycle = RollCycles.MINUTELY
    )

    Bitfinex().market(pair("btc", "usd"))
            .orders()
            .doOnNext { log.debug { "bitfinex/btc-usd : ${it.compact()}" } }
            .map { it.toByteArray() }
            .forEach { rawStreamWriter.write(it) }

    readLine()

}