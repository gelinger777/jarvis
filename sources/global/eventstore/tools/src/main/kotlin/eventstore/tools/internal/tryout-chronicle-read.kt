package eventstore.tools.internal

import net.openhft.chronicle.queue.RollCycles
import util.app

internal fun main(args: Array<String>) {
    val ch = queue("/Users/vach/workspace/jarvis/dist/data/test-cycles", RollCycles.MINUTELY)

    val tl = ch.createTailer()

    var lastCycle = -1;

    while (true) {

        val cycle = tl.cycle()
        val index = tl.index()
        val message = tl.readText() ?: break

        if (lastCycle != cycle) {
            // this is the way to calculate first index of the cycle...
            app.log.info("cycle : " + RollCycles.MINUTELY.toIndex(cycle, 0))
            lastCycle = cycle
        }
        app.log.info("$cycle : $index : $message")
    }


}