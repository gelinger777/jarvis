package util

import com.tars.util.exceptions.ExceptionUtils.executeSilent
import extensions.logger
import rx.schedulers.Schedulers.from
import util.exceptionUtils.executeMandatory
import util.exceptionUtils.onUnrecoverableFailure
import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.ForkJoinPool.commonPool
import java.util.concurrent.TimeUnit


object cpu {

    val log by logger()

    private val threadFactory = { runnable: Runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread
    }


    object executors {
        val fj = commonPool()
        val io = newCachedThreadPool(threadFactory)
    }

    object schedulers {
        val fj = from(executors.fj)
        val io = from(executors.io)
    }

    // lifecycle

    fun init() {
        onUnrecoverableFailure { throwable -> close() }
    }

    fun close() {
        // common pool doesn't need shutdown

        executors.io.shutdown()

        log.debug("waiting for pools to shut down")

        executeMandatory { executors.io.awaitTermination(1, TimeUnit.MINUTES) }
        executeMandatory { executors.fj.awaitQuiescence(1, TimeUnit.MINUTES) }
    }

    // sleep

    fun bearSleep(millis: Long) {
        executeSilent { TimeUnit.MILLISECONDS.sleep(millis) }
    }

    fun bearSleep(value: Long, unit: TimeUnit) {
        executeSilent { unit.sleep(value) }
    }
}
