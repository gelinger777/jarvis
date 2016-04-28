//package util.chronicle
//
//import util.misc.RefCountTask
//import util.cleanupTasks
//import util.global.logger
//import java.lang.Thread.`yield`
//import java.lang.Thread.currentThread
//import java.util.concurrent.CopyOnWriteArraySet
//
//internal object watchersEventLoop {
//    val log by logger()
//    val activeWatchers = CopyOnWriteArraySet<Watcher>()
//
//    val eventLoopTask = RefCountTask("chronicle-watcher-event-loop", {
//        log.debug("event loop started")
//
//        cleanupTasks.add("chronicle-watcher-event-loop", { this.release() }, 1)
//
//        // while not interrupted
//        while (!currentThread().isInterrupted) {
//            activeWatchers.forEach { it.checkAndEmit() }
//            `yield`() // check out LongPauser in chronicle core project
//        }
//
//        cleanupTasks.remove("chronicle-watcher-event-loop")
//        log.debug("event loop completed")
//    })
//
//    fun add(watcher: Watcher) {
//        activeWatchers.add(watcher)
//        eventLoopTask.increment()
//        log.debug("started watching {}", watcher.path)
//    }
//
//    fun remove(watcher: Watcher) {
//        activeWatchers.remove(watcher)
//        eventLoopTask.decrement()
//        log.debug("stopped watching {}", watcher.path)
//    }
//
//    fun release() {
//        activeWatchers.clear()
//        eventLoopTask.reset()
//    }
//
//}