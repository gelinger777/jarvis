package eventstore.tools.internal

import com.amazonaws.services.s3.model.S3ObjectSummary
import com.google.protobuf.MessageLite
import eventstore.tools.io.bytes.BytesWriter
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.ChronicleQueueBuilder
import net.openhft.chronicle.queue.ExcerptAppender
import net.openhft.chronicle.queue.RollCycle
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue
import java.nio.file.Paths

fun queue(path: String, rollCycle: RollCycle): SingleChronicleQueue {
    return ChronicleQueueBuilder
            .single(path)
            .rollCycle(rollCycle)
            .build()
}

fun BytesWriter.write(proto: MessageLite){
    write(proto.toByteArray())
}

fun BytesWriter.write(string: String){
    write(string.toByteArray(Charsets.UTF_8))
}


fun ExcerptAppender.write(bytes: ByteArray): Long {
    return this.apply { writeBytes(Bytes.wrapForRead(bytes)) }.lastIndexAppended()
}


//fun RollCycles.firstIdOfCycleContaining(timestamp: Long): Int {
//    return 0
//}

fun S3ObjectSummary.fileName(): String {
    return Paths.get(key).fileName.toString()
}