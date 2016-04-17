package util

import global.logger

/**
 * Utility class for exception handling and boilerplate elimination.

 * note : logger with file appender must be configured for ExceptionUtils class.
 */
object exceptionUtils {

    /**
     * Appropriate file appender shall be defined in logging configuration file.
     */
    private val log by logger("exceptions")

    // wtf methods

    /**
     * All overloaded wtf() methods are logging the provided data (if any) to a file and kill the process, these
     * represent use cases that shall never happen in production.

     * P.S. WTF stands for (what a terrible failure)
     */
    fun <T> wtf(): T {
        throw reportAndKill(WTFException())
    }

    /**
     * All overloaded wtf() methods are logging the provided data (if any) to a file and kill the process, these
     * represent use cases that shall never happen in production.

     * P.S. WTF stands for (what a terrible failure)
     */
    fun <T> wtf(message: String): T {
        throw reportAndKill(WTFException(message))
    }

    /**
     * All overloaded wtf() methods are logging the provided data (if any) to a file and kill the process, these
     * represent use cases that shall never happen in production.

     * P.S. WTF stands for (what a terrible failure)
     */
    fun <T> wtf(cause: Throwable): T {
        throw reportAndKill(WTFException(cause))
    }

    /**
     * All overloaded wtf() methods are logging the provided data (if any) to a file and kill the process, these
     * represent use cases that shall never happen in production.

     * P.S. WTF stands for (what a terrible failure)
     */
    fun <T> wtf(cause: Throwable, message: String): T {
        throw reportAndKill(WTFException(message, cause))
    }

    // failure callbacks

    private val failureCallbacks = mutableListOf<(Throwable) -> Unit>()

    /**
     * Sets callback to be executed when unrecoverable exception is about to kill the process. This can be used to notify
     * about critical failure or for graceful shutdown.
     */
    @Synchronized fun onUnrecoverableFailure(task: (Throwable) -> Unit) {
        failureCallbacks.add(task)
    }

    // logging to file

    /**
     * Logs an exception to general error log and returns without throwing it further.
     */
    private fun report(cause: Throwable): Throwable {
        log.warn("unexpected exception", cause)
        return cause
    }

    /**
     * Logs an exception to general error log and returns without throwing it further.
     */
    private fun report(cause: Throwable, message: String): Throwable {
        log.warn(message, cause)
        return cause
    }

    private @Synchronized fun reportAndKill(cause: Throwable): Throwable {
        log.error("unrecoverable exception", cause)

        // execute registered callbacks
        failureCallbacks.forEach { it.invoke(cause) }

        // kill process
        System.exit(-1)

        return cause
    }

    // runnable execution exception handling

    /**
     * Executes runnable, if any exception is thrown logs it and ignores.
     */
    fun executeSilent(block: () -> Unit) {
        try {
            block.invoke()
        } catch (ignored: Throwable) {
            report(ignored)
        }

    }

    /**
     * Executes runnable, if any exception is thrown logs it and rethrows.
     */
    fun execute(block: () -> Unit) {
        try {
            block.invoke()
        } catch (cause: Throwable) {
            throw report(cause)
        }

    }

    /**
     * Executes runnable, if any exception is thrown logs it, executes callbacks if any, AND KILLS THE PROCESS.
     */
    fun executeMandatory(block: () -> Unit) {
        try {
            block.invoke()
        } catch (cause: Throwable) {
            throw reportAndKill(WTFException(cause))
        }

    }

    // callable execution exception handling

    /**
     * Executes callable, wraps in Option, if any exception is thrown logs it and returns empty option.
     */
    fun <T> executeAndGetSilent(callable: () -> T): Option<T> {
        try {
            return Option.ofNullable(callable.invoke())
        } catch (cause: Throwable) {
            report(cause)
            return Option.empty<T>()
        }

    }

    /**
     * Executes callable, if callable returns null or any exception is thrown logs it, and rethrows.
     */
    fun <T> executeAndGet(callable: () -> T): T {
        try {
            val result = callable.invoke()

            if (result != null) {
                return result
            } else {
                throw report(RuntimeException("callable returned null"))
            }
        } catch (cause: Throwable) {
            throw report(cause)
        }
    }

    /**
     * Executes callable, if callable returns null or any exception is thrown logs it, executes callbacks if any, AND
     * KILLS THE PROCESS.
     */
    fun <T> executeAndGetMandatory(callable: () -> T): T {
        try {
            val result = callable.invoke()

            if (result != null) {
                return result
            } else {
                throw RuntimeException("callable returned null")
            }
        } catch (cause: Throwable) {
            throw reportAndKill(WTFException(cause))
        }

    }

    // marker class

    /**
     * WTFException represents an unrecoverable error, a logical error that is not considered to ever happen in the system.
     */
    private class WTFException : RuntimeException {

        constructor() : super() {
        }

        constructor(message: String) : super(message) {
        }

        constructor(message: String, cause: Throwable) : super(message, cause) {
        }

        constructor(cause: Throwable) : super(cause) {
        }
    }
}

