package util

import rx.schedulers.Schedulers.from
import util.global.executeMandatory
import util.global.executeSilent
import util.global.logger
import util.global.toClosure
import util.misc.RefCountTask
import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.ForkJoinPool.commonPool
import java.util.concurrent.TimeUnit


object cpu {

    val log by lazyLogger("cpu")

    private val threadFactory = { runnable: Runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread
    }

    init {
        log.debug("init")

        cleanupTasks.internalAdd({
            log.debug("shutdown");

            // common pool doesn't need shutdown
            executors.io.shutdown()

            log.debug("waiting for pools to shut down")

            executeMandatory { executors.io.awaitTermination(1, TimeUnit.MINUTES) }
            executeMandatory { executors.fj.awaitQuiescence(1, TimeUnit.MINUTES) }
        }, key = "cpu")
    }


    object executors {
        val fj = commonPool()
        val io = newCachedThreadPool(threadFactory)
    }

    object schedulers {
        val fj = from(executors.fj)
        val io = from(executors.io)
    }

    fun refCountTask(name: String, task: () -> Unit, timeout: Long = 10000): RefCountTask {
        return RefCountTask(name, task, timeout)
    }

    @JvmStatic fun refCountTask(name: String, task: Runnable, timeout: Long): RefCountTask {
        return RefCountTask(name, task.toClosure(), timeout)
    }

    @JvmStatic fun refCountTask(name: String, task: Runnable): RefCountTask {
        return RefCountTask(name, task.toClosure(), 10000)
    }

    // sleep

    @JvmStatic fun sleep(millis: Long) {
        executeSilent { TimeUnit.MILLISECONDS.sleep(millis) }
    }

    @JvmStatic fun sleep(value: Long, unit: TimeUnit) {
        executeSilent { unit.sleep(value) }
    }
}
