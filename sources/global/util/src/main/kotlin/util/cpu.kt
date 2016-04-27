package util

import global.logger
import global.toClosure
import rx.schedulers.Schedulers.from
import util.exceptionUtils.executeSilent
import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.ForkJoinPool.commonPool
import java.util.concurrent.TimeUnit


object cpu {

    val log by logger("cpu")

    private val threadFactory = { runnable: Runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread
    }

    init {
        log.info("init")

        cleanupTasks.internalAdd("cpu", {
            log.info("shutdown");

            // common pool doesn't need shutdown
            executors.io.shutdown()

            log.debug("waiting for pools to shut down")

            exceptionUtils.executeMandatory { executors.io.awaitTermination(1, TimeUnit.MINUTES) }
            exceptionUtils.executeMandatory { executors.fj.awaitQuiescence(1, TimeUnit.MINUTES) }
        })
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
