package util


import com.tars.util.exceptions.ExceptionUtils.wtf
import kotlin.concurrent.thread

class RefCountTask(val name: String, val task: () -> Unit, val terminationTimeout: Long = 10000) {

    var thread = Option.empty<Thread>()

    val toggle = RefCountToggle(
            start = {
                thread
                        .ifPresent { wtf("thread is already running") }
                        .ifNotPresentTakeCompute {
                            thread(name = name, block = task, isDaemon = true, start = true)
                        }
            },
            stop = {
                thread
                        .ifNotPresent { wtf("thread is not available") }
                        .ifPresent {
                            it.interrupt()
                            if (it !== Thread.currentThread()) {
                                it.join(terminationTimeout)
                            }
                        }
                        .clear<Thread>()
            }
    )

    fun increment() {
        toggle.increment()
    }

    fun decrement() {
        toggle.decrement()
    }

}