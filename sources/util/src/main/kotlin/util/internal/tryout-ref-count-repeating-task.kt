package util.internal

import util.app
import util.misc.RefCountRepeatingTask

internal fun main(args: Array<String>) {
    val refCountTask = RefCountRepeatingTask(
            "test",
            { app.log.info { "working" } },
            1000
    )

    readLine()

    refCountTask.increment()

    readLine()

    refCountTask.increment()

    readLine()

    refCountTask.decrement()

    readLine()

    refCountTask.decrement()

    readLine()


}