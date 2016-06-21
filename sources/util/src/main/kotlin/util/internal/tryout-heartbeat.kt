package util.internal

import util.heartBeat

internal fun main(args: Array<String>) {
    heartBeat.add("test-beat", 3000, { println("we are fucked") })

    Thread.sleep(2000)

    heartBeat.add("test-beat", 3000, { println("we are fucked") })

    heartBeat.beat("test-beat")

    Thread.sleep(4000)

    heartBeat.remove("test-beat")
}
