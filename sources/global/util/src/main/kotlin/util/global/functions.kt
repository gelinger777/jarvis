package util.global

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

/**
 * This is only here to trick compiler, pretend to return whatever type is required,
 * while this method will never return...
 */
fun <T> whatever(block: () -> Unit): T {
    block.invoke()
    throw IllegalStateException("")
}

fun <T> notImplemented(): T {
    return whatever { wtf("this shouldn't happen") }
}

fun <T> wth(): T {
    return whatever { wtf("this code should be unreachable") }
}

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

fun occasionally(probability: Double, task: () -> Unit) {
    if (ThreadLocalRandom.current().nextDouble() > probability) {
        task.invoke()
    }
}