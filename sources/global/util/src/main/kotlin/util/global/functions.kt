package util.global

import java.util.function.Consumer

/**
 * Will execute a block of code and throw a runtime exception.
 * This is only placeholder for required return values.
 */
internal fun <T> whatever(block: () -> Unit): T {
    block.invoke()
    throw IllegalStateException("")
}

fun <T> notImplemented(): T {
    return whatever { wtf("not implemented") }
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
