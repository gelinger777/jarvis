package eventstore.tools.internal

import eventstore.tools.StreamUploader

internal fun main(args: Array<String>) {
    val uploader = StreamUploader(
            source = "/Users/vach/workspace/jarvis/dist/data/uuid/",
            bucket = "jarvis-historical",
            folder = "some/test/stream"
    )

    println(uploader.source)

    uploader.start()

    readLine()
}
