package eventstore.tools.internal

import eventstore.tools.net.QueuePoller

internal fun main(args: Array<String>) {
    QueuePoller(
            localPath = "/Users/vach/workspace/jarvis/dist/data/historical-test",
            remotePath = "test", bucket = "jarvis-history"
    )

    readLine()
}
