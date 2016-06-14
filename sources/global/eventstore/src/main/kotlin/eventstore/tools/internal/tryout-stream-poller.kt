package eventstore.tools.internal

import eventstore.tools.StreamPoller

internal fun main(args: Array<String>) {
    val poller = StreamPoller(
            destination = "/Users/vach/workspace/jarvis/dist/data/uuid/",
            bucket = "jarvis-historical",
            folder = "test"
    )


    poller.start()

    readLine()

}
