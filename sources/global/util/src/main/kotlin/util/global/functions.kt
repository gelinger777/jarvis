package util.global

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

fun <T> consumer(consumer: Consumer<T>): (T) -> Unit {
    return { value: T -> consumer.accept(value) }
}

fun runnable(runnable: Runnable): () -> Unit {
    return { runnable.run() }
}

fun roundDown5(d: Double): Double {
    return Math.floor(d * 1e5) / 1e5
}

fun roundDown3(d: Double): Double {
    return Math.floor(d * 1e3) / 1e3
}
