package usage

import com.tars.util.Util
import eventstore.storage
import global.logger
import rx.Observer

fun main(args: Array<String>) {
    val path = Util.absolutePathOf("data/temp")

    val stream = storage.eventStream(path)

//    for (value in 0..100) {
//        stream.write(toByteArray(value))
//    }

    val subscription = stream.streamRealtime().subscribe(
            object : Observer<ByteArray> {
                val log by logger("PrintObserver")

                override fun onNext(value: ByteArray) {
                    log.debug("next value {}", fromByteArray(value))
                }

                override fun onError(error: Throwable) {
                    log.error("error was emitted", error)
                }

                override fun onCompleted() {
                    log.debug("completed")
                }
            }
    )

    readLine()

    subscription.unsubscribe()

    readLine()
}

//fun printObserver(): Observer<ByteArray> {
//    return
//}
