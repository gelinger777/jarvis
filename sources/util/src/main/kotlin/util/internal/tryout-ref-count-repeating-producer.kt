package util.internal

import util.app
import util.misc.RefCountRepeatingProducer
import java.util.*

internal fun main(args: Array<String>) {
    val pr = RefCountRepeatingProducer(
            "string producer",
            { UUID.randomUUID().toString() },
            1000
    )

    pr.stream().forEach { app.log.info { "produced : $it" } }

    pr.increment()
    pr.increment()

    readLine()

    pr.decrement()
    pr.decrement()

    readLine()
}