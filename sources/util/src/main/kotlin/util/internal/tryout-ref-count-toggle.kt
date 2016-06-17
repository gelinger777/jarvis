package util.internal

import util.app
import util.misc.RefCountToggle

internal fun main(args: Array<String>) {
    val tg = RefCountToggle(
            { app.log.info { "on" } },
            { app.log.info { "off" } }
    )

    tg.increment()
    tg.increment()
    tg.increment()

    tg.decrement()
    tg.decrement()
    tg.decrement()
    tg.decrement()
}