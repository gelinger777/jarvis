package eventstore.tools.internal

import com.google.protobuf.MessageLite
import eventstore.tools.StreamWriter
import net.openhft.chronicle.queue.ChronicleQueueBuilder
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue

fun queue(path: String, rollupCycle: RollCycles): SingleChronicleQueue {
    return ChronicleQueueBuilder
            .single(path)
            .rollCycle(rollupCycle)
            .build()
}

fun StreamWriter.write(proto: MessageLite){
    write(proto.toByteArray())
}

fun StreamWriter.write(string: String){
    write(string.toByteArray(Charsets.UTF_8))
}