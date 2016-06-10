package engine.internal.eventstore.chronicle

import net.openhft.chronicle.queue.ChronicleQueueBuilder
import net.openhft.chronicle.queue.RollCycles

internal fun main(args: Array<String>) {
    val builder = ChronicleQueueBuilder
            .single("/Users/vach/workspace/jarvis/dist/data/temp")
            .blockSize(1024)
            .rollCycle(RollCycles.MINUTELY)


    val ch = builder.build()


    val tl = ch.createTailer()

    var lastCycle = -1;





    while (true) {

        // todo : time delay problem, events written to the cycle N might belong to cycle N-1
        // todo : order stream does not make sense without streaming snapshot

        // todo : find out how to calculate right id for given long timestamp
        val message = tl.readText() ?: break
        val cycle = tl.cycle()
        if (lastCycle != cycle) {
            println("cycle : " + RollCycles.MINUTELY.toIndex(tl.cycle(), 0))
            lastCycle = cycle
        }
        println("${tl.cycle()} : ${tl.index()} : $message")
    }



}


fun RollCycles.firstIdOfCycleContaining(timestamp: Long): Int {
    return 0
}