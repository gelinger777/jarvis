package eventstore.tools.internal

import eventstore.tools.net.EventStreamUploader

internal fun main(args: Array<String>) {
    EventStreamUploader(
            localPath = "/Users/vach/workspace/jarvis/dist/data/test",
            remotePath = "test",
            bucket = "jarvis-history"
    )

    readLine()
}
