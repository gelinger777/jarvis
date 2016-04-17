package util


import com.tars.util.exceptions.ExceptionUtils.wtf
import global.addShutdownHook
import kotlin.concurrent.thread

class RefCountTask(val name: String, val task: () -> Unit, val terminationTimeout: Long = 10000) {

    var thread: Thread? = null

    val toggle = RefCountToggle(
            start = {
                Option.ofNullable(thread)
                        .ifNotPresent {
                            thread = thread(name = name, block = task, isDaemon = true, start = true)
                        }
                        .ifPresent { wtf("thread is already running") }
            },
            stop = {
                Option.of(thread)
                        .ifPresent {
                            it.interrupt()
                            if (it !== Thread.currentThread()) {
                                it.join(terminationTimeout)
                            }
                            thread = null
                        }
                        .ifNotPresent { wtf("thread is not available") }
            }
    )

    fun increment() {
        toggle.increment()
    }

    fun decrement() {
        toggle.decrement()
    }

    fun stopOnShutdown(): RefCountTask {
        addShutdownHook { toggle.reset() }
        return this
    }
}