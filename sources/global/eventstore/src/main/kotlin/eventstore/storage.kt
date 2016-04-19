package eventstore

import global.computeIfAbsent
import global.logger
import net.openhft.chronicle.Chronicle
import net.openhft.chronicle.ChronicleQueueBuilder
import util.cleanupTasks

internal object storage {
    val log by logger()

    val chronicles = mutableMapOf<String, Chronicle>()

    val watchers = mutableMapOf<String, Watcher>()

    val streams = mutableMapOf<String, EventStream>()

    fun getChronicle(path: String): Chronicle {
        return chronicles.computeIfAbsent(path, {
            // create chronicle instance
            val chronicle = ChronicleQueueBuilder.indexed(it).small().build()

            // register cleanup code
            cleanupTasks.add("chronicle:$path", { chronicle.close() })

            // return new instance
            chronicle
        })
    }

    fun getWatcher(path: String): Watcher {
        return watchers.computeIfAbsent(path, { Watcher(it) })
    }

    fun getStream(path: String): EventStream {
        return streams.computeIfAbsent(path, { EventStream(it) })
    }

}