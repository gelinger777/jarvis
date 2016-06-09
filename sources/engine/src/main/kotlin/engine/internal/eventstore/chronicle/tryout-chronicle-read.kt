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

    while (true) {

        val message = tl.readText() ?: break

        println(message)
    }

}
