package eventstore.tools.internal

import eventstore.tools.net.EventStreamPoller

internal fun main(args: Array<String>) {
    EventStreamPoller(
            localPath = "/Users/vach/workspace/jarvis/dist/data/historical-test",
            remotePath = "test", bucket = "jarvis-history"
    )

    readLine()
}
