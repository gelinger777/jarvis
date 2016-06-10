package eventstore.tools.internal

import net.openhft.chronicle.queue.RollCycles

internal fun main(args: Array<String>) {
    val ch = queue("/Users/vach/workspace/jarvis/dist/data/temp", RollCycles.MINUTELY)

    val tl = ch.createTailer()

    var lastCycle = -1;

    while (true) {

        val message = tl.readText() ?: break
        val cycle = tl.cycle()
        if (lastCycle != cycle) {
            println("cycle : " + RollCycles.MINUTELY.toIndex(tl.cycle(), 0))
            lastCycle = cycle
        }
        println("${tl.cycle()} : ${tl.index()} : $message")
    }



}