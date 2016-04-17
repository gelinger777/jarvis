package usage

import eventstore.storage

fun main(args: Array<String>) {
    val stream = storage.getStream("/Users/vach/workspace/projects/jarvis/data/temp")

    stream.write(byteArrayOf(0, 1, 1))
}