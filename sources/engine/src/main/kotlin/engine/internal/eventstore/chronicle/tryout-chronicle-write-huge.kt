package engine.internal.eventstore.chronicle

import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.ChronicleQueueBuilder

internal fun main(args: Array<String>) {
    val ch = ChronicleQueueBuilder
            .single("/Users/vach/workspace/jarvis/dist/data/temp")
//            .rollCycle(RollCycles.MINUTELY)
            .build()

    val ap = ch.createAppender()

    val data = byteArrayOf(127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127)

    for(i in 0..40000000){
        ap.writeBytes(Bytes.wrapForRead(data))
    }

    readLine()

}
