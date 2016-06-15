package eventstore.tools.internal

import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.transfer.Upload
import com.google.protobuf.MessageLite
import eventstore.tools.io.ESWriter
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.ChronicleQueueBuilder
import net.openhft.chronicle.queue.ExcerptAppender
import net.openhft.chronicle.queue.RollCycle
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue
import java.io.File

fun queue(path: String, rollCycle: RollCycle): SingleChronicleQueue {
    return ChronicleQueueBuilder
            .single(path)
            .rollCycle(rollCycle)
            .build()
}

fun ESWriter.write(proto: MessageLite){
    write(proto.toByteArray())
}

fun ESWriter.write(string: String){
    write(string.toByteArray(Charsets.UTF_8))
}


fun ExcerptAppender.write(bytes: ByteArray): Long {
    return this.apply { writeBytes(Bytes.wrapForRead(bytes)) }.lastIndexAppended()
}


//fun RollCycles.firstIdOfCycleContaining(timestamp: Long): Int {
//    return 0
//}


fun S3ObjectSummary.fileName(): String {
    return File(key).name
}

fun Upload.notCompleted(): Boolean {
    return !isDone
}