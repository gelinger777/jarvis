package eventstore

import global.addShutdownHook
import global.computeIfAbsent
import net.openhft.chronicle.Chronicle
import net.openhft.chronicle.ChronicleQueueBuilder

internal object storage {

    val chronicles = mutableMapOf<String, Chronicle>()

    val watchers = mutableMapOf<String, Watcher>()

    val streams = mutableMapOf<String, EventStream>()

    fun getChronicle(path: String): Chronicle {
        return chronicles.computeIfAbsent(path, {
            // create chronicle instance
            val chronicle = ChronicleQueueBuilder.indexed(it).small().build()

            // register cleanup code
            addShutdownHook { chronicle.close() }

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