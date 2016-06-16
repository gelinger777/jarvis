package engine.internal.io

import common.global.json
import common.global.order
import engine.io.readers.OrderStreamReader
import engine.io.writers.OrderStreamWriter
import eventstore.tools.io.EventStreamReader
import eventstore.tools.io.EventStreamWriter
import net.openhft.chronicle.queue.RollCycles
import proto.common.Order
import util.app

/**
 * Record orders from bitfinex using eventstore tools.
 */
internal fun main(args: Array<String>) {

    val rawStreamWriter = EventStreamWriter(
            path = "/Users/vach/workspace/jarvis/dist/data/temp",
            cycles = RollCycles.MINUTELY
    )

    val orderStreamWriter = OrderStreamWriter(rawStreamWriter)

    orderStreamWriter.write(order(Order.Side.BID, 42.0, 42.0, 100))
    orderStreamWriter.write(order(Order.Side.ASK, 42.0, 42.0, 200))
    orderStreamWriter.write(order(Order.Side.BID, 42.0, 42.0, 300))

    val rawStreamReader = EventStreamReader(
            path = "/Users/vach/workspace/jarvis/dist/data/temp",
            rollCycle = RollCycles.MINUTELY
    )

    OrderStreamReader(rawStreamReader).stream().forEach {
        app.log.info("read : " + it.json())
    }

}