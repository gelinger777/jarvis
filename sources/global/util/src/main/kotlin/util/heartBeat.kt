package util

import util.cpu.executors.io
import util.global.duration
import util.global.getOptional
import util.global.logger
import util.misc.RefCountRepeatingTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object heartBeat {
    private val log = logger("heartbeat")
    private val registry = ConcurrentHashMap<String, Pulse>()
    private val watchDogTask = RefCountRepeatingTask(
            name = "heartbeat-watchdog",
            task = {
                // iterate over all monitored instances
                for (pulse in registry.values) {
                    log.debug { "checking : ${pulse.name}" }
                    // filter those who violated the timeout
                    if ((System.currentTimeMillis() - pulse.lastBeat.get()) > pulse.timeout) {
                        // schedule the callback execution
                        log.warn { "heartbeat violation of ${pulse.name}, running the callback" }
                        io.execute(pulse.callback)

                        if (!pulse.keepAlive) {
                            stop(pulse.name)
                        }
                    }
                }
            },
            delay = 30 * 1000
    )

    fun start(name: String, timeout: Long, callback: () -> Unit, keepAlive: Boolean = false) {
        if (registry.containsKey(name)) {
            log.warn { "attempt to add existing heartbeat $name" }
        } else {
            registry.put(name, Pulse(name, timeout, callback, keepAlive))
            watchDogTask.increment()
            log.info { "started heartbeat $name with maximum timeout of ${duration(timeout)}" }
        }
    }

    fun stop(name: String) {
        if (registry.containsKey(name)) {
            registry.remove(name)
            watchDogTask.decrement()
            log.info { "stopped heartbeat $name" }
        } else {
            log.warn { "attempt to stop unregistered heartbeat $name" }
        }
    }

    fun beat(name: String) {
        registry.getOptional(name).ifPresent {
            it.lastBeat.set(app.time())
            log.debug { "beat at $name" }
        }
    }

    data class Pulse(val name: String, val timeout: Long, val callback: () -> Unit, val keepAlive: Boolean, val lastBeat: AtomicLong = AtomicLong(System.currentTimeMillis()))
}


