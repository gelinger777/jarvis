package util.global

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

fun <T> consumer(consumer: Consumer<T>): (T) -> Unit {
    return { value: T -> consumer.accept(value) }
}

fun runnable(runnable: Runnable): () -> Unit {
    return { runnable.run() }
}


fun Double.roundDown1(): Double {
    return Math.floor(this * 1e1) / 1e1
}

fun Double.roundDown2(): Double {
    return Math.floor(this * 1e2) / 1e2
}

fun Double.roundDown3(): Double {
    return Math.floor(this * 1e3) / 1e3
}

fun Double.roundDown5(): Double {
    return Math.floor(this * 1e5) / 1e5
}

fun Double.roundDown7(): Double {
    return Math.floor(this * 1e7) / 1e7
}

fun Long.dateTime(): String {
    return DateTimeFormatter.ofPattern("YYY-MM-dd HH:mm:ss").format(ZonedDateTime.ofInstant (Instant.ofEpochMilli(this), ZoneOffset.UTC))
}

fun size(bytes: Int): String {
    return size(bytes.toLong())
}

fun size(bytes: Long): String {

    if (bytes < 1000) {
        return "$bytes Bytes"
    }
    // less than 1 MB
    else if (bytes < 1000 * 1000) {
        return "${(bytes / 1000.0).roundDown1()} KB"
    }
    // less than 1 GB
    else if (bytes < 1000 * 1000 * 1000) {
        return "${(bytes / (1000 * 1000.0)).roundDown1()} MB"
    }
    // more than 1 GB
    else {
        return "${(bytes / (1000 * 1000 * 1000.0)).roundDown1()} GB"
    }
}

fun duration(timeout: Long): String {
    if (timeout < 1000) {
        return "$timeout ms"
    }
    // less than 1 Minute
    else if (timeout < 60 * 1000) {
        return "${(timeout / 1000.0).roundDown1()} seconds"
    }
    // less than 1 Hour
    else if (timeout < 60 * 60 * 1000) {
        return "${(timeout / (60 * 1000.0)).roundDown1()} minutes"
    }

    // less than a day
    else if (timeout < 24 * 60 * 60 * 1000) {
        return "${(timeout / (60 * 60 * 1000.0)).roundDown1()} hours"
    } else {
        return "${(timeout / (24 * 60 * 60 * 1000.0)).roundDown1()} days"
    }
}

/**
 * Execute block with probability...
 */
fun withProbability(probability: Double, block: () -> Unit) {
    if (ThreadLocalRandom.current().nextDouble() < probability) {
        block.invoke()
    }
}

fun sleepLoop(
        condition: () -> Boolean = { false },
        task: () -> Unit,
        delay: Long = 1.seconds()) {

    while (!condition.invoke()) {
        task.invoke()

        try {
            Thread.sleep(delay)
        } catch(e: InterruptedException) {
            break
        }
    }
}


//    /**
//     * Create a runnable that when run will apply provided name to the thread while its being executed...
//     */
//    fun task(name: String, block: () -> Unit): Runnable {
//        return Runnable {
//            val thread = Thread.currentThread()
//            val oldName = thread.name
//            thread.name = name
//
//            block.invoke()
//
//            thread.name = oldName
//        }
//    }

/**
 * Create a runnable that when run will apply provided name to the thread while its being executed...
 */
fun named(name: String, block: () -> Unit): () -> Unit {
    return {
        val thread = Thread.currentThread()
        val oldName = thread.name
        thread.name = name

        block.invoke()

        thread.name = oldName
    }
}

fun sleepUntilInterrupted() {
    while (true) {
        try {
            Thread.sleep(Long.MAX_VALUE)
        } catch(interruption: InterruptedException) {
            break
        }
    }
}