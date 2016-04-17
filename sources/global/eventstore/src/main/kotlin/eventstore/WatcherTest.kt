package eventstore

import global.logger
import rx.Observer
import java.util.*

fun main(args: Array<String>) {

    // todo use temp folder
    val subscription = storage
            .getWatcher("/Users/vach/workspace/projects/jarvis/data/temp")
            .stream()
            .subscribe(printObserver())

    readLine()

    subscription.unsubscribe()

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