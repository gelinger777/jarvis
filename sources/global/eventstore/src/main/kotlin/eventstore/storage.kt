package eventstore

import global.computeIfAbsent
import global.logger
import net.openhft.chronicle.Chronicle
import net.openhft.chronicle.ChronicleQueueBuilder
import util.cleanupTasks

object storage {
    val log by logger()

    private val chronicles = mutableMapOf<String, Chronicle>()
    private val watchers = mutableMapOf<String, Watcher>()
    private val streams = mutableMapOf<String, EventStream>()

    internal fun chronicle(path: String): Chronicle {
        return chronicles.computeIfAbsent(path, {
            // create chronicle instance
            val chronicle = ChronicleQueueBuilder.indexed(it).small().build()

            // register cleanup code
            cleanupTasks.add("chronicle:$path", { chronicle.close() })

            // return new instance
            chronicle
        })
    }

    internal fun watcher(path: String): Watcher {
        return watchers.computeIfAbsent(path, { Watcher(it) })
    }

    fun eventStream(path: String): EventStream {
        return streams.computeIfAbsent(path, { EventStream(it) })
    }

}