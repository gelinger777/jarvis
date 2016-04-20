package usage

import com.tars.util.Util
import eventstore.storage
import global.consoleStream
import util.cpu

@Volatile var flag = false

fun main(args: Array<String>) {

    val path = Util.absolutePathOf("data/temp")

    val stream = storage.eventStream(path)


    consoleStream().subscribe({
        when (it) {
            "append" -> {
                cpu.executors.io.submit {
                    val index = stream.write(toByteArray(42))
                    println("[$index] : 42")
                }
            }
        }
    })
}
