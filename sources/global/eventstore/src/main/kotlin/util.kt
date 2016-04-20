import net.openhft.chronicle.ExcerptAppender
import net.openhft.chronicle.ExcerptTailer

internal fun readFrame(tailer: ExcerptTailer): ByteArray {
    // read next message size
    val result = ByteArray(tailer.readInt())
    // read message content
    tailer.read(result)
    tailer.finish()
    return result
}

internal fun writeFrame(appender: ExcerptAppender, data: ByteArray) : Long{
    // current index we write to
    val index = appender.index()
    // calculate total message size
    val msgSize = 4 + data.size
    appender.startExcerpt(msgSize.toLong())
    // write length of the message
    appender.writeInt(data.size)
    // write message content
    appender.write(data)
    appender.finish()

    return index
}