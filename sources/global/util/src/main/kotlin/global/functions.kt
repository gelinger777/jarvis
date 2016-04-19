package global

//fun addShutdownHook(closure: () -> Unit) {
//    Runtime.getRuntime().addShutdownHook(Thread(closure))
//}
//
//fun addShutdownHook(runnable: Runnable) {
//    Runtime.getRuntime().addShutdownHook(Thread(runnable))
//}

fun <T> whatever(block: () -> Unit): T {
    block.invoke()
    throw IllegalStateException()
}