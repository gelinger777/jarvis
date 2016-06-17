package engine.internal

import util.app
import util.global.report
import util.global.wtf

internal fun main(args: Array<String>) {
    app.log.debug { "this is for app to initialize" }

    report("something")

    wtf("this is to cause critical error in the system")
}