//package usage
//
//import com.tars.util.Util
//import eventstore.server.storage
//import util.global.logger
//import rx.Observer
//import java.nio.ByteBuffer
//
///**
// * Use this to test eventstore.
// */
//internal fun main(args: Array<String>) {
//    val path = Util.absolutePathOf("data/temp")
//
//    val stream = storage.eventStream(path)
//
////    for (value in 0..100) {
////        stream.write(toByteArray(value))
////    }
//
//    val subscription = stream.streamRealtime(99).subscribe(
//            object : Observer<ByteArray> {
//                val log by logger("PrintObserver")
//
//                override fun onNext(value: ByteArray) {
//                    log.debug("next value {}", fromByteArray(value))
//                }
//
//                override fun onError(error: Throwable) {
//                    log.error("error was emitted", error)
//                }
//
//                override fun onCompleted() {
//                    log.debug("completed")
//                }
//            }
//    )
//
//    readLine()
//
//    subscription.unsubscribe()
//
//    readLine()
//}
//
//internal fun fromByteArray(bytes: ByteArray): Int {
//    return ByteBuffer.wrap(bytes).int
//}