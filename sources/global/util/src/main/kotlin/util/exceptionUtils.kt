package util

import rx.subjects.PublishSubject
import util.global.logger
import util.global.whatever

/**
 * Utility class for exception handling and boilerplate elimination.
 *
 * All overloaded wtf() methods are logging the provided data (if any) to a file and kill the process, these
 * represent use cases that shall never happen in production. This is following fail fast principle.
 *
 * P.S. WTF stands for (what a terrible failure)
 *
 * note : logger with file appender must be configured for ExceptionUtils class.
 */
object exceptionUtils {

    /**
     * Appropriate file appender shall be defined in logging configuration file.
     */
    private val log by logger("exceptions")

    private val unrecoverableErrorStream = PublishSubject.create<Throwable>()

    /**
     * Register a listener for the unrecoverable errors.
     */
    fun onUnrecoverableFailure(listener: (Throwable) -> Unit) {
        unrecoverableErrorStream.subscribe({
            executeSilent { listener.invoke(it) }
        })
    }

    // wtf methods

    fun wtf() {
        reportAndKill(WTFException())
    }

    fun wtf(message: String) {
        reportAndKill(WTFException(message))
    }

    fun wtf(cause: Throwable) {
        reportAndKill(WTFException(cause))
    }

    fun wtf(cause: Throwable, message: String) {
        reportAndKill(WTFException(message, cause))
    }

    // logging to file

    /**
     * Logs an exception to general error log and returns without throwing it further.
     */
    fun report(cause: Throwable, message: String = "unexpected exception"): Throwable {
        log.info(message, cause)
        return cause
    }

    private fun reportAndKill(cause: Throwable) {
        // write log to a file
        log.error("unrecoverable error", cause)

        // acknowledge all the failure subscribers
        unrecoverableErrorStream.onNext(cause);

        // kill process
        System.exit(-1)
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
            reportAndKill(WTFException(cause))
        }

    }

    // callable execution exception handling

    /**
     * Executes callable, wraps in Option, if any exception is thrown logs it and returns empty option.
     * Use case : failure is an expected execution flow.
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
     * Use case : failure is not expected but not fatal for the application.
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
     * Use case : failure is not expected and is fatal for the application.
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
            return whatever { reportAndKill(WTFException(cause)) }
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

