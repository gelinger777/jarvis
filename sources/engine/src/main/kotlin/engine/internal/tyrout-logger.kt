package engine.internal

import util.global.logger

internal fun main(args: Array<String>) {
    val log = logger("test")

    log.trace("Trace Message!");
    log.debug("Debug Message!");
    log.info("Info Message!");
    log.warn("Warn Message!");
    log.error("Error Message!");
}