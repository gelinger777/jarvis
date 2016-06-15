package util.global

import util.Option
import util.app

/**
 * WTFException represents an unrecoverable error, a logical error that is not considered to ever happen in the system.
 */
internal class WTFException : RuntimeException {

    constructor() : super() {
    }

    constructor(message: String) : super(message) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }

    constructor(cause: Throwable) : super(cause) {
    }
}

// wtf methods

fun wtf(): Nothing {
    reportAndKill(WTFException())
}

fun wtf(message: String): Nothing {
    reportAndKill(WTFException(message))
}

fun wtf(cause: Throwable): Nothing {
    reportAndKill(WTFException(cause))
}

fun wtf(cause: Throwable, message: String): Nothing {
    reportAndKill(WTFException(message, cause))
}

fun notImplemented(): Nothing {
    wtf("not implemented")
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
        report(cause)
        throw cause
    }

}

/**
 * Executes runnable, if any exception is thrown logs it, executes callbacks if any, AND KILLS THE PROCESS.
 */
fun executeMandatory(block: () -> Unit) {
    try {
        block.invoke()
    } catch (cause: Throwable) {
        wtf(cause)
    }

}

// supplier execution exception handling

/**
 * Executes supplier, wraps in Option, if any exception is thrown logs it and returns empty option.
 */
fun <T> executeAndGetSilent(supplier: () -> T): Option<T> {
    try {
        return Option.ofNullable(supplier.invoke())
    } catch (cause: Throwable) {
        report(cause)
        return Option.empty<T>()
    }
}


/**
 * Executes runnable, if any exception is thrown logs it and rethrows.
 */
fun <T> executeAndGet(supplier: () -> T): T? {
    try {
        return supplier.invoke()
    } catch (cause: Throwable) {
        report(cause)
        throw cause
    }
}


/**
 * Executes supplier, if supplier returns null or any exception is thrown logs it, executes callbacks if any, AND
 * KILLS THE PROCESS.
 */
fun <T> executeAndGetMandatory(supplier: () -> T): T {
    try {
        val result = supplier.invoke()

        if (result != null) {
            return result
        } else {
            throw RuntimeException("supplier returned null")
        }
    } catch (cause: Throwable) {
        wtf(cause)
    }
}

/**
 * Try convert nullable to non nullable or fail.
 */
fun <T> tryGet(arg: T?): T {
    if (arg != null) {
        return arg
    } else {
        throw NullPointerException()
    }
}

/**
 * Try convert nullable to non nullable or die.
 */
fun <T> getMandatory(arg: T?): T {
    return arg ?: wtf("not null is expected")
}

// reporting

/**
 * Logs an exception to general error log and returns without throwing it further.
 */
fun report(cause: Throwable) {
    app.reportedErrors.onNext(cause)
}

/**
 * Logs an exception to general error log and returns without throwing it further.
 */
fun report(message: String) {
    report(RuntimeException(message))
}

private fun reportAndKill(cause: Throwable): Nothing {
    // acknowledge all the failure subscribers
    app.unrecoverableErrors.onNext(cause);

    // kill process (let the cleanup tasks run)
    System.exit(-1)

    // this is just to trick the compiler (vm is killed at this po
    throw cause;
}
