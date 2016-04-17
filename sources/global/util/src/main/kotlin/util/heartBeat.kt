package util

import com.tars.util.validation.Validator.condition
import global.logger
import util.cpu.executors.io
import java.lang.Thread.currentThread
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object heartBeat {
    private val log by logger()
    private val registry = ConcurrentHashMap<String, Pulse>()
    private val watchDogTask = RefCountTask("heartbeat-watchdog", {
        log.info("heartbeat watchdog started")

        while (!currentThread().isInterrupted) {
            // iterate over all monitored instances
            for (pulse in registry.values) {
                log.trace("checking : {}", pulse.name)
                // filter those who violated the timeout
                if (System.currentTimeMillis() - pulse.lastBeat.get() < pulse.timeout) {
                    // schedule the callback execution
                    log.warn("heartbeat violation : {}", pulse.name)
                    io.execute(pulse.callback)
                    stop(pulse.name)
                }
            }

            // support interruption
            try {
                TimeUnit.SECONDS.sleep(1)
            } catch (ignored: InterruptedException) {
                // heartbeat watchdog is not needed anymore (ref count = 0)
                break
            }

        }

        log.info("heartbeat watchdog completed")
    })

    fun start(name: String, timeout: Long, callback: () -> Unit) {
        condition(!registry.containsKey(name), "another heartbeat with the same name [$name] is already registered")

        registry.put(name, Pulse(name, timeout, callback))
        watchDogTask.increment()
        log.info("started heartbeat : $name")
    }

    fun stop(name: String) {
        condition(registry.contains(name), "no heartbeat is registered under [$name]")

        registry.remove(name)
        watchDogTask.decrement()

        log.info("stopped heartbeat : $name")
    }

    fun beat(name: String) {
        val pulse = registry[name] ?: throw IllegalArgumentException("no heartbeat is registered under [$name]")
        pulse.lastBeat.set(System.currentTimeMillis())

        log.debug("beat on : $name")
    }

    private data class Pulse(val name: String, val timeout: Long, val callback: () -> Unit, var lastBeat: AtomicLong = AtomicLong(System.currentTimeMillis()))
}


