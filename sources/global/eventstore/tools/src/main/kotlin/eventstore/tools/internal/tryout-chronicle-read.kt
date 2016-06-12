package eventstore.tools.internal

import net.openhft.chronicle.queue.RollCycles

internal fun main(args: Array<String>) {
    val ch = queue("/Users/vach/workspace/jarvis/dist/data/test-cycles", RollCycles.MINUTELY)

    val tl = ch.createTailer()

    var lastCycle = -1;

    while (true) {

        val cycle = tl.cycle()
        val index = tl.index()
        val message = tl.readText() ?: break

        if (lastCycle != cycle) {
            println("cycle : " + RollCycles.MINUTELY.toIndex(cycle, 0)) // todo this is the way to calculate first index of the cycle...
            lastCycle = cycle
        }
        println("$cycle : $index : $message")
    }



}