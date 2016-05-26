package util.misc


import util.Option
import util.global.wtf
import javax.annotation.concurrent.ThreadSafe
import kotlin.concurrent.thread

@ThreadSafe
class RefCountTask(val name: String, val task: () -> Unit, val terminationTimeout: Long = 10000) {

    var thread: Thread? = null

    val toggle = RefCountToggle(
            on = {
                if (thread == null) {
                    thread = thread(name = name, block = task, isDaemon = true, start = true)
                } else {
                    wtf("thread is already running")
                }
            },
            off = {
                val t = thread

                if (t != null) {
                    t.interrupt()
                    if (t !== java.lang.Thread.currentThread()) {
                        t.join(terminationTimeout)
                    }
                    thread = null
                } else {
                    wtf("thread is not available")
                }
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