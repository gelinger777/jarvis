package util

import util.cpu.executors.io
import util.global.condition
import util.global.logger
import util.misc.RefCountSchTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object heartBeat {
    private val log by logger("heartbeat")
    private val registry = ConcurrentHashMap<String, Pulse>()
    private val watchDogTask = RefCountSchTask(
            name = "heartbeat-watchdog",
            task = {
                // iterate over all monitored instances
                for (pulse in registry.values) {
                    log.trace("checking : {}", pulse.name)
                    // filter those who violated the timeout
                    if ((System.currentTimeMillis() - pulse.lastBeat.get()) > pulse.timeout) {
                        println(System.currentTimeMillis())
                        println(pulse.lastBeat.get())
                        println(pulse.timeout)


                        // schedule the callback execution
                        log.warn("heartbeat violation : {}", pulse.name)
                        io.execute(pulse.callback)
                        stop(pulse.name)
                    }
                }
            },
            delay = 1000
    )

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

    data class Pulse(val name: String, val timeout: Long, val callback: () -> Unit, val lastBeat: AtomicLong = AtomicLong(System.currentTimeMillis()))
}


