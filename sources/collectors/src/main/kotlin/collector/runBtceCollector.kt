package collector

import client.btce.Btce
import collector.common.startCollectorFor
import util.app
import util.global.seconds
import util.global.sleepLoop
import util.heartBeat

fun main(args: Array<String>) {
    app.log.info { "starting Btce collector" }
    startCollectorFor(Btce())

    sleepLoop(
            task = { heartBeat.status() },
            delay = 3.seconds()
    )
}