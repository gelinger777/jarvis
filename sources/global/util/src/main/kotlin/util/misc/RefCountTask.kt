package util.misc


import util.Option
import util.global.wtf
import javax.annotation.concurrent.ThreadSafe
import kotlin.concurrent.thread


/**
 * Reference Counting Task ensures that with the first positive reference a daemon thread will be
 * executing defined task, and that this thread will be interrupted when reference count gets to 0.
 * Provided task must support interruption.
 */
@ThreadSafe
class RefCountTask(private val name: String, private val task: () -> Unit, private val terminationTimeout: Long = 10000) {

    private var thread: Thread? = null

    private val toggle = RefCountToggle(
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

    fun forceStart() {
        toggle.forceOn()
    }

    fun forceStop() {
        toggle.forceOff()
    }

    fun isStarted(): Boolean {
        return thread != null
    }

    fun isAlive(): Boolean {
        return Option.ofNullable(thread)
                .filter { it.isAlive }
                .isPresent()
    }

    fun isNotAlive(): Boolean {
        return !isAlive()
    }

    fun isCurrentThread(): Boolean {
        return Option.ofNullable(thread)
                .filter { it == Thread.currentThread() }
                .isPresent()
    }
}