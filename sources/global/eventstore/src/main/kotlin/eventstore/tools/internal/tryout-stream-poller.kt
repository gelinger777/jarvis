package eventstore.tools.internal

import eventstore.tools.net.ESPoller

internal fun main(args: Array<String>) {
    val poller = ESPoller(
            destination = "/Users/vach/workspace/jarvis/dist/data/uuid/",
            bucket = "jarvis-historical",
            folder = "test"
    )


    poller.start()

    readLine()

}
