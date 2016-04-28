package eventstore.server

import util.global.computeIfAbsent
import util.global.logger

internal object storage {
    val log by logger()

    //    private val chronicles = mutableMapOf<String, Chronicle>()
    //    private val watchers = mutableMapOf<String, Watcher>()
    private val streams = mutableMapOf<String, EventStream>()

    //    internal fun chronicle(path: String): Chronicle {
    //        return chronicles.computeIfAbsent(path, {
    //            // create chronicle instance
    //            val chronicle = ChronicleQueueBuilder.indexed(it).small().build()
    //
    //            // register cleanup code
    //            cleanupTasks.add("chronicle:$path", { chronicle.close() })
    //
    //            // return new instance
    //            chronicle
    //        })
    //    }
    //
    //    internal fun watcher(path: String): Watcher {
    //        return watchers.computeIfAbsent(path, { Watcher(it) })
    //    }

    fun eventStream(path: String): EventStream {
        return streams.computeIfAbsent(path, { EventStream(toAbsolutePath(it)) })
    }

    private fun toAbsolutePath(relative: String): String {
        return appConfig.storageRoot + relative
    }

}