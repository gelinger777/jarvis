package util.internal

import util.app
import util.misc.RefCountTask

internal fun main(args: Array<String>) {
    val refCountTask = RefCountTask(
            "test",
            {
                while (true) {
                    app.log.info { "working" }
                    try {
                        Thread.sleep(1000)
                    }catch(e: Exception){
                        break;
                    }
                }
            }
    )

//    readLine()

    refCountTask.increment()

//    readLine()

    refCountTask.increment()

//    readLine()

    refCountTask.decrement()

//    readLine()

    refCountTask.decrement()

//    readLine()

}