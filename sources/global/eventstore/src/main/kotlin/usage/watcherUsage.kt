package usage

import eventstore.storage
import global.logger
import rx.Observer
import util.cleanupTasks
import java.util.*

fun main(args: Array<String>) {

    val subscription = storage
            .watcher("/Users/vach/workspace/projects/jarvis/data/temp")
            .stream()
            .subscribe(printObserver())

    readLine()

    cleanupTasks.printExecutionInOrder()

    readLine()
}

fun printObserver(): Observer<ByteArray> {
    return object : Observer<ByteArray> {
        val log by logger("PrintObserver")

        override fun onNext(value: ByteArray) {
            log.debug("next value {}", Arrays.toString(value))
        }

        override fun onError(error: Throwable) {
            log.error("error was emitted", error)
        }

        override fun onCompleted() {
            log.debug("completed")
        }
    }
}