package util.global

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
fun <T> whatever(block: () -> Unit): T {
    block.invoke()
    throw IllegalStateException()
}