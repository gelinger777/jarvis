package engine.internal.io

import common.global.json
import common.global.order
import engine.io.order.OrderReader
import engine.io.order.OrderWriter
import eventstore.tools.io.BytesReader
import eventstore.tools.io.BytesWriter
import net.openhft.chronicle.queue.RollCycles
import proto.common.Order
import util.app

/**
 * Record orders from bitfinex using eventstore tools.
 */
internal fun main(args: Array<String>) {

    val bw = BytesWriter(
            path = "/Users/vach/workspace/jarvis/dist/data/temp/order",
            cycles = RollCycles.MINUTELY
    )

    val orderStreamWriter = OrderWriter(bw)

    orderStreamWriter.write(order(Order.Side.BID, 42.0, 42.0, 100))
    orderStreamWriter.write(order(Order.Side.ASK, 42.0, 42.0, 200))
    orderStreamWriter.write(order(Order.Side.BID, 42.0, 42.0, 300))

    val br = BytesReader(
            path = "/Users/vach/workspace/jarvis/dist/data/temp/order",
            rollCycle = RollCycles.MINUTELY
    )

    OrderReader(br).stream().forEach {
        app.log.info("read : " + it.json())
    }

}