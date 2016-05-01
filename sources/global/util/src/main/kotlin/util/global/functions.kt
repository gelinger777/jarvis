package util.global

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.function.Consumer

//fun addShutdownHook(closure: () -> Unit) {
//    Runtime.getRuntime().addShutdownHook(Thread(closure))
//}
//
//fun addShutdownHook(runnable: Runnable) {
//    Runtime.getRuntime().addShutdownHook(Thread(runnable))
//}

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

fun <T : Message.Builder> T.readFromFS(): T {
    return this.apply {
        executeMandatory {
            val log = util.global.logger("util.global")

            log.info("getting location of configuration")
            val path = System.getProperty("config")

            condition(notNullOrEmpty(path), "system property was not provided")

            log.info("reading configuration from file system")
            val json = FileUtils.readFileToString(File(path))

            log.info("merging configuration")
            JsonFormat.parser().merge(json, this)
        }
    }
}