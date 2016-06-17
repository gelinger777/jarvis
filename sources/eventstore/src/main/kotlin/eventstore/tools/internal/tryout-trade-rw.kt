package eventstore.tools.internal

import common.global.json
import common.global.trade
import eventstore.tools.io.bytes.BytesReader
import eventstore.tools.io.bytes.BytesWriter
import eventstore.tools.io.trade.TradeReader
import eventstore.tools.io.trade.TradeWriter
import net.openhft.chronicle.queue.RollCycles
import util.app

/**
 * Record trades from bitfinex using eventstore tools.
 */
internal fun main(args: Array<String>){

    val bw = BytesWriter(
            path = "/Users/vach/workspace/jarvis/dist/data/temp/trade",
            cycles = RollCycles.MINUTELY
    )

    val tw = TradeWriter(bw)

    tw.write(trade(42.0, 42.0, 100))
    tw.write(trade(42.0, 42.0, 200))
    tw.write(trade(42.0, 42.0, 300))

    val br = BytesReader(
            path = "/Users/vach/workspace/jarvis/dist/data/temp/trade",
            rollCycle = RollCycles.MINUTELY
    )

    TradeReader(br).stream().forEach {
        app.log.info("read : " + it.json())
    }

}