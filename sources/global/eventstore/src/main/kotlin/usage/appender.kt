package usage

import com.tars.util.Util
import eventstore.storage

@Volatile var flag = false

fun main(args: Array<String>) {

    val path = Util.absolutePathOf("data/temp")

    val stream = storage.eventStream(path)

    for (num in 0..10) {
        stream.write(toByteArray(num))
    }


//
//    consoleStream().subscribe({
//        when (it) {
//            "append some" -> {
//                cpu.executors.io.submit {
//
//
//                }
//            }
//        }
//    })
}
