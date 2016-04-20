package usage

import com.tars.util.Util
import eventstore.storage

fun main(args: Array<String>) {

    val path = Util.absolutePathOf("data/temp")

    val chronicle = storage.chronicle(path)

    val eventStream = storage.eventStream(path)

//    eventStream.streamExisting().subscribe({ println(fromByteArray(it)) })


}
