package engine.internal

import util.global.logger

internal fun main(args: Array<String>) {
    val log = logger("test")

//    println(System.getProperty("log4j.configurationFile"))

//    for (i in 0..1000) {
//        log.info("some stuff $i", UUID.randomUUID())
//    }

    // todo figure out how to use lambda logging http://stackoverflow.com/questions/37767444/kotlin-logging-with-lambda-parameters
//    log.debug("random {}", { UUID.randomUUID() })

    log.trace("Trace Message!");
    log.debug("Debug Message!");
    log.info("Info Message!");
    log.warn("Warn Message!");
    log.error("Error Message!");
}