package global

fun addShutdownHook(closure: () -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread(closure))
}

fun addShutdownHook(runnable: Runnable) {
    Runtime.getRuntime().addShutdownHook(Thread(runnable))
}