package eventstore.tools

import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.queue.ExcerptAppender
import net.openhft.chronicle.queue.RollCycles

fun ExcerptAppender.write(bytes: ByteArray): Long {
    return this.apply { writeBytes(Bytes.wrapForRead(bytes)) }.lastIndexAppended()
}


fun RollCycles.firstIdOfCycleContaining(timestamp: Long): Int {
    return 0
}