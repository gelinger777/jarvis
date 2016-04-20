package global

//fun addShutdownHook(closure: () -> Unit) {
//    Runtime.getRuntime().addShutdownHook(Thread(closure))
//}
//
//fun addShutdownHook(runnable: Runnable) {
//    Runtime.getRuntime().addShutdownHook(Thread(runnable))
//}

/**
 * Will execute a block of code and throw a runtime exception.
 * This is only imitating a generic return value.
 */
fun <T> whatever(block: () -> Unit): T {
    block.invoke()
    throw IllegalStateException()
}