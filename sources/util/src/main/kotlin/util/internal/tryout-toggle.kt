package util.internal

import util.app
import util.misc.Toggle

internal fun main(args: Array<String>) {
    val tg = Toggle(
            { app.log.info { "on" } },
            { app.log.info { "off" } }
    )

    tg.on()
    tg.off()
}