package util

import com.tars.util.validation.Validator.condition
import global.logger
import global.toClosure
import util.exceptionUtils.executeSilent


object cleanupTasks {
    val log by logger()
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
    @Synchronized fun add(key: String, task: () -> Unit, priority: Int = 0) {
        condition(priority >= 0)
        tasks.put(key, priority to task)
    }

    /**
     * Tasks with higher priorities execute first.
     * Key must be unique to the task, if a task already exists under that key it will be replaced.
     * Priority must be positive integer or zero.
     */
    @JvmStatic @Synchronized fun add(key: String, task: Runnable, priority: Int = 0) {
        condition(priority >= 0)
        tasks.put(key, priority to task.toClosure())
    }


    @Synchronized fun remove(key:String):Boolean{
        return tasks.remove(key) != null
    }

    /**
     * For internal usage, internal tasks will execute only after all user tasks.
     * Key must be unique to the task, if a task already exists under that key it will be replaced.
     * Priority must be positive integer or zero.
     */
    @Synchronized internal fun internalAdd(key: String, task: () -> Unit, priority: Int = 0) {
        condition(priority >= 0)
        internalTasks.put(key, priority to task)
    }

    fun printExecutionInOrder() {
        tasks.entries.asSequence()
                .sortedBy { -it.value.first }
                .forEach {
                    System.err.println("${it.key}|${it.value.first}")
                }

        internalTasks.entries.asSequence()
                .sortedBy { -it.value.first }
                .forEach {
                    System.err.println("${it.key}|${it.value.first}")
                }
    }
}

fun main(args: Array<String>) {
    cleanupTasks.add("task 1", {}, 1)
    cleanupTasks.add("task 2", {}, 2)
    cleanupTasks.add("task 3", {}, 3)
    cleanupTasks.add("task 3.1", {}, 3)
    cleanupTasks.add("task 3.2", {}, 3)
    cleanupTasks.add("task 4", {}, 4)
    cleanupTasks.add("task 4", {}, 4)
    cleanupTasks.add("task", {}, 4)

    if(net.http != null && net.socket != null){
        cleanupTasks.printExecutionInOrder()
    }

    exceptionUtils.wtf()
}
