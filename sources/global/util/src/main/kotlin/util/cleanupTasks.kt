package util

import com.tars.util.validation.Validator.condition
import global.addShutdownHook
import global.logger
import global.toClosure
import util.exceptionUtils.executeSilent


object cleanupTasks {
    val log by logger()
    val tasks = mutableListOf<Pair<Int, () -> Unit>>()
    internal val internalTasks = mutableListOf<Pair<Int, () -> Unit>>()

    init {
        addShutdownHook {
            log.info("executing cleanup tasks")

            tasks.asSequence()
                    .sortedBy { -it.first }
                    .forEach {
                        executeSilent { it.second.invoke() }
                    }

            internalTasks.asSequence()
                    .sortedBy { -it.first }
                    .forEach {
                        executeSilent { it.second.invoke() }
                    }

        }
    }

    /**
     * Tasks with higher priorities execute first.
     * Priority must be positive integer or zero.
     */
    fun add(task: () -> Unit, priority: Int = 0) {
        condition(priority >= 0)
        tasks.add(priority to task)
    }

    /**
     * Tasks with higher priorities execute first.
     * Priority must be positive integer or zero.
     */
    @JvmStatic fun add(task: Runnable, priority: Int = 0) {
        condition(priority >= 0)
        tasks.add(priority to task.toClosure())
    }

    /**
     * For internal usage, internal tasks will execute only after all user tasks.
     */
    internal fun internalAdd(task: () -> Unit, priority: Int = 0) {
        condition(priority >= 0)
        internalTasks.add(priority to task)
    }
}

