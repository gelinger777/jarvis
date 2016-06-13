package util.logging

import org.apache.logging.log4j.util.Supplier

class Logger(val logger: org.apache.logging.log4j.Logger) : org.apache.logging.log4j.Logger by logger {

    inline fun info(crossinline supplier: () -> String) {
        logger.info(Supplier { supplier.invoke() })
    }

    inline fun debug(crossinline supplier: () -> String) {
        logger.debug(Supplier { supplier.invoke() })
    }

    inline fun warn(crossinline supplier: () -> String) {
        logger.warn(Supplier { supplier.invoke() })
    }

    inline fun error(crossinline supplier: () -> String) {
        logger.error(Supplier { supplier.invoke() })
    }

    inline fun trace(crossinline supplier: () -> String) {
        logger.trace(Supplier { supplier.invoke() })
    }
}