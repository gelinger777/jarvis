package util.misc

import util.global.notInterrupted
import java.util.concurrent.TimeUnit.MILLISECONDS

class RefCountSchTask(val name: String, val task: () -> Unit, val delay: Long, val terminationTimeout: Long = 10000) {
    val scheduledTask = {
        while (Thread.currentThread().notInterrupted()) {
            task.invoke()
            try {
                MILLISECONDS.sleep(delay)
            } catch (any: Exception) {
                break
            }
        }
    }

    val refCountTask = RefCountTask(name, scheduledTask, terminationTimeout)

    fun increment() {
        refCountTask.increment()
    }

    fun decrement() {
        refCountTask.decrement()
    }

    fun reset() {
        refCountTask.reset()
    }

    fun isAlive(): Boolean {
        return refCountTask.isAlive()
    }

    fun isCurrentThread(): Boolean {
        return refCountTask.isCurrentThread()
    }

}