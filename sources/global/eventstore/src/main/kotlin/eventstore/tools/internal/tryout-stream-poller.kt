package eventstore.tools.internal

import eventstore.tools.net.ESPoller

internal fun main(args: Array<String>) {
    val poller = ESPoller(
            localRoot = "/Users/vach/workspace/jarvis/dist/data/historical-test",
            bucket = "jarvis-historical", folder = "test"
    )

    poller.start()

    readLine()
}
