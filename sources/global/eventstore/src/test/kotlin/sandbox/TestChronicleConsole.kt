package sandbox


import com.tars.util.Util.absolutePathOf
import net.openhft.chronicle.Chronicle
import net.openhft.chronicle.ChronicleQueueBuilder
import util.cpu
import util.cpu.bearSleep
import util.exceptionUtils.executeSilent
import java.util.*


val srcPath = absolutePathOf("data/temp/src")
val rplPath = absolutePathOf("data/temp/rpl")

val src = ChronicleQueueBuilder
        .indexed(srcPath)
        .small()
        .source()
        .bindAddress("localhost", 1234)
        .build()

val rpl = ChronicleQueueBuilder
        .indexed(rplPath)
        .small()
        .build()

var sink: Chronicle? = null

fun main(args: Array<String>) {

    val scanner = Scanner(System.`in`)
    var keepTaking = true

    while (keepTaking) {

        println("awaiting for input")
        val input = scanner.nextLine()

        when (input) {
            "start" -> startThread()
            "stop" -> stopThread()
            "append" -> appendSingle()

            "printsrc" -> printAll(src)
            "printrpl" -> printAll(rpl)
            "printsink" -> {
                val queue = sink;
                if (queue != null) printAll(queue)
            }

            "createrpl" -> createReplica()

            "size" -> println(src.lastIndex())

            "close" -> {
                flag = false
                keepTaking = false
            }
            else -> println("unknown input")
        }
    }

    src.close()
}

@Volatile var flag = true

private fun startThread() {
    Thread {
        executeSilent {
            val appender = src.createAppender()
            val bytes = byteArrayOf(0, 1, 0, 1, 0)
            val length = bytes.size
            flag = true
            while (flag) {
                bearSleep(1000)
                appender.startExcerpt(100)
                appender.writeInt(length)
                appender.write(bytes)
                appender.finish()
            }
            appender.close()
        }
    }.start()
}

fun stopThread() {
    flag = false
}

fun appendSingle() {
    executeSilent {
        val appender = src.createAppender()
        val bytes = byteArrayOf(0, 1, 0, 1, 0)
        val length = bytes.size

        appender.startExcerpt(100)
        appender.writeInt(length)
        appender.write(bytes)
        appender.finish()
        appender.close()
    }
}

fun printAll(chronicle: Chronicle) {
    executeSilent {
        val tailer = chronicle.createTailer()

        while (true) {
            if (tailer.nextIndex()) {
                val data = ByteArray(tailer.readInt())
                tailer.finish()
                println("${tailer.index()} : $data")
            } else {
                println("no more data")
                break
            }
        }

        tailer.close()
    }
}

fun createReplica() {
    sink = ChronicleQueueBuilder
            .sink(rpl)
            .connectAddress("localhost", 1234)
            .build();
}

