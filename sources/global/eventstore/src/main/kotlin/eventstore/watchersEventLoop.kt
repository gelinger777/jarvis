package eventstore

import global.logger
import util.RefCountTask
import java.lang.Thread.`yield`
import java.lang.Thread.currentThread
import java.util.concurrent.CopyOnWriteArraySet

internal object watchersEventLoop {
    val log by logger()
    val activeWatchers = CopyOnWriteArraySet<Watcher>()

    val eventLoopTask = RefCountTask("chronicle-watcher-event-loop",{
        log.debug("event loop started")

        // while not interrupted
        while(!currentThread().isInterrupted){
            activeWatchers.forEach { it.checkAndEmit() }
            `yield`()
        }

        log.debug("event loop completed")
    })

    fun add(watcher: Watcher) {
        activeWatchers.add(watcher)
        eventLoopTask.increment()
        log.debug("started watching {}", watcher.path)
    }

    fun remove(watcher: Watcher) {
        activeWatchers.remove(watcher)
        eventLoopTask.decrement()
        log.debug("stopped watching {}", watcher.path)
    }

}