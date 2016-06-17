package eventstore.tools.internal

import eventstore.tools.net.QueueUploader

internal fun main(args: Array<String>) {
    QueueUploader(
            localPath = "/Users/vach/workspace/jarvis/dist/data/test",
            remotePath = "test",
            bucket = "jarvis-history"
    )

    readLine()
}
