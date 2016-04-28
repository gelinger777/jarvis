package util.misc


import util.Option
import util.global.wtf
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

    fun reset() {
        toggle.reset()
    }

    fun isAlive(): Boolean {
        return Option.ofNullable(thread)
                .filter { it.isAlive }
                .isPresent()
    }

    fun isCurrentThread(): Boolean {
        return Option.ofNullable(thread)
                .filter { it == Thread.currentThread() }
                .isPresent()
    }
}