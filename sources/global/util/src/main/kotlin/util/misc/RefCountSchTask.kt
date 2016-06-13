package util.misc

import util.cpu
import util.global.executeSilent
import util.global.notInterrupted

/**
 * Scheduled Reference Counting Task is extended version of RefCountTask, except it expects the task,
 * to complete without interruption, in which case after specified delay it will reschedule the same
 * task again (while reference count is positive).
 */
class RefCountSchTask(
        val name: String,
        val task: () -> Unit,
        @Volatile var delay: Long,
        val terminationTimeout: Long = 10000) {

    private val scheduledTask = {
        while (Thread.currentThread().notInterrupted()) {
            executeSilent(task)
            cpu.sleep(delay)
        }
    }


    private val refCountTask = RefCountTask(name, scheduledTask, terminationTimeout)

    fun increment() {
        refCountTask.increment()
    }

    fun decrement() {
        refCountTask.decrement()
    }

    fun forceStart() {
        refCountTask.forceStart()
    }

    fun forceStop() {
        refCountTask.forceStop()
    }

    fun isStarted(): Boolean {
        return refCountTask.isStarted()
    }

    fun isAlive(): Boolean {
        return refCountTask.isAlive()
    }

    fun isNotAlive(): Boolean {
        return refCountTask.isNotAlive()
    }

    fun isCurrentThread(): Boolean {
        return refCountTask.isCurrentThread()
    }

    fun delay(delay: Long) {
        this.delay = delay
    }

}