package eventstore.server

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import org.apache.commons.io.FileUtils
import proto.eventstore.EventStoreConfig
import util.global.condition
import util.global.executeMandatory
import util.global.notNullOrEmpty
import java.io.File

val config = EventStoreConfig.newBuilder().readFromFS().build()

fun <T : Message.Builder> T.readFromFS(): T {
    return this.apply {
        executeMandatory {
            val path = System.getProperty("config")
            condition(notNullOrEmpty(path), "system property was not provided")
            val json = FileUtils.readFileToString(File(path))
            JsonFormat.parser().merge(json, this)
        }
    }
}

fun main(args: Array<String>) {
    println(config)
}
