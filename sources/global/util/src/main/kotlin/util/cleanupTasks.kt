package util

import util.global.condition
import util.global.executeSilent
import util.global.logger
import util.global.toClosure
import java.util.*

object cleanupTasks {
    val log by logger("maid")
    val tasks = mutableMapOf<String, Pair<Int, () -> Unit>>()
    internal val internalTasks = mutableMapOf<String, Pair<Int, () -> Unit>>()

    init {
        Runtime.getRuntime().addShutdownHook(Thread({
            log.info("executing cleanup tasks")

            // user code tasks
            tasks.entries.asSequence()
                    .sortedBy { -it.value.first }
                    .forEach {
                        log.debug("cleanup : ${it.key}")
                        executeSilent { it.value.second.invoke() }
                    }

            // internal cleanup
            internalTasks.entries.asSequence()
                    .sortedBy { -it.value.first }
                    .forEach {
                        log.debug("cleanup : ${it.key}")
                        executeSilent { it.value.second.invoke() }
                    }
        }))
    }

    /**
     * Tasks with higher priorities execute first.
     * Key must be unique to the task, if a task already exists under that key it will be replaced.
     * Priority must be positive integer or zero.
     */
    @Synchronized fun add(task: () -> Unit, priority: Int = 0, key: String = UUID.randomUUID().toString()) {
        condition(priority >= 0)
        tasks.put(key, priority to task)
    }

    /**
     * Tasks with higher priorities execute first.
     * Key must be unique to the task, if a task already exists under that key it will be replaced.
     * Priority must be positive integer or zero.
     */
    @JvmStatic @Synchronized fun add(task: Runnable, priority: Int = 0, key: String = UUID.randomUUID().toString()) {
        condition(priority >= 0)
        tasks.put(key, priority to task.toClosure())
    }

    @Synchronized fun remove(key: String): Boolean {
        return tasks.remove(key) != null
    }

    /**
     * For internal usage, internal tasks will execute only after all user tasks.
     * Key must be unique to the task, if a task already exists under that key it will be replaced.
     * Priority must be positive integer or zero.
     */
    @Synchronized internal fun internalAdd(task: () -> Unit, priority: Int = 0, key: String = UUID.randomUUID().toString()) {
        condition(priority >= 0)
        internalTasks.put(key, priority to task)
    }

//    fun printExecutionInOrder() {
//        tasks.entries.asSequence()
//                .sortedBy { -it.value.first }
//                .forEach {
//                    System.err.println("${it.key}|${it.value.first}")
//                }
//
//        internalTasks.entries.asSequence()
//                .sortedBy { -it.value.first }
//                .forEach {
//                    System.err.println("${it.key}|${it.value.first}")
//                }
//    }
}
