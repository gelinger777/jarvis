package util

import com.tars.util.validation.Validator.condition
import global.addShutdownHook
import util.exceptionUtils.executeSilent


object cleanupTasks {

    init {
        addShutdownHook {
            tasks.asSequence()
                    .sortedBy { -it.first }
                    .forEach {
                        executeSilent { it.second.invoke() }
                    }
        }
    }

    val tasks = mutableListOf<Pair<Int, () -> Unit>>()

    /**
     * Tasks with higher priorities execute first.
     */
    fun addWithPriority(priority: Int, task: () -> Unit) {
        condition(priority >= 0)
        tasks.add(priority to task)
    }

    /**
     * Add task with no priority (priority is 0).
     */
    fun add(task: () -> Unit) {
        tasks.add(0 to task)
    }

    /**
     * For internal use only.
     */
    internal fun doLast(task: () -> Unit) {
        tasks.add(-1 to task)
    }

}
//
//fun main(args: Array<String>) {
// todo try to make all resources clean up on shutdown
//    cleanupTasks.add(1, { println("task 1") })
//    cleanupTasks.add(4, { println("task 3") })
//    cleanupTasks.add(2, { println("task 2") })
//    cleanupTasks.add(0, { println("task 0") })
//    cleanupTasks.add(-1, { println("task -1") })
//    cleanupTasks.add(1, { println("task 1") })
//}


