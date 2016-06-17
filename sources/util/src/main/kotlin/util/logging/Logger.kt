package util.logging

import org.apache.logging.log4j.util.Supplier

class Logger(val logger: org.apache.logging.log4j.Logger) : org.apache.logging.log4j.Logger by logger {

    fun info(supplier: () -> String) = logger.info(Supplier { supplier.invoke() })

    fun debug(supplier: () -> String) = logger.debug(Supplier { supplier.invoke() })

    fun warn(supplier: () -> String) = logger.warn(Supplier { supplier.invoke() })

    fun error(supplier: () -> String) = logger.error(Supplier { supplier.invoke() })

    fun trace(supplier: () -> String) = logger.trace(Supplier { supplier.invoke() })
}