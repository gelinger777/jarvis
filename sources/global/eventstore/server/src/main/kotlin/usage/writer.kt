//package usage
//
//import com.tars.util.Util
//import eventstore.server.storage
//import util.global.consoleStream
//import util.cpu
//import java.nio.ByteBuffer
//
///**
// * Use this to test eventstore.
// */
//internal fun main(args: Array<String>) {
//
//    val path = Util.absolutePathOf("data/temp")
//
//    val stream = storage.eventStream(path)
//
//
//    consoleStream().subscribe({
//        when (it) {
//            "append" -> {
//                cpu.executors.io.submit {
//                    val index = stream.write(toByteArray(42))
//                    println("[$index] : 42")
//                }
//            }
//        }
//    })
//
//}
//
//internal fun toByteArray(value: Int): ByteArray {
//    return ByteBuffer.allocate(4).putInt(value).array()
//}
