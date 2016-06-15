package eventstore.tools.internal

import eventstore.tools.net.ESUploader
import util.app

internal fun main(args: Array<String>) {
    val uploader = ESUploader(
            source = "/Users/vach/workspace/jarvis/dist/data/test/",
            bucket = "jarvis-historical",
            folder = "test"
    )

    app.log.info(uploader.source)

    uploader.start()

    readLine()

    uploader.stop()
}
