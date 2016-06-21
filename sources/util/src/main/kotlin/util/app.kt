package util

import rx.subjects.PublishSubject
import util.global.*
import java.util.concurrent.ThreadLocalRandom

object app {
    val log = logger("app")
    val exceptionLogger = logger("exc")
    val profile: String

    // todo : make this internal when jetbrains fixes the method not found bug
    internal val reportedErrors = PublishSubject.create<Throwable>()
    internal val unrecoverableErrors = PublishSubject.create<Throwable>()

    init {
        ensurePropertiesAreProvided(
                "log.path",
                "profile"
        )

        val logs = property("log.path")
        profile = property("profile")

        when (profile) {
            "dev" -> {
                reportedErrors.forEach {
                    exceptionLogger.warn("unexpected error", it)
                }

                unrecoverableErrors.forEach {
                    exceptionLogger.error("unrecoverable error", it)
                }
            }
            "prod" -> {
                // use exceptions to notify developer (possible to notify specific people depending on the package)

                reportedErrors.forEach {
                    exceptionLogger.warn ("unexpected error", it)

                    net.mail.send(
                            subject = "reported error",
                            message = it.stackTraceAsString(),
                            destination = "vachagan.balayan@gmail.com",
                            senderName = "Jarvis",
                            senderAddress = "jarvis@everywhere.com"
                    )
                }

                unrecoverableErrors.forEach {
                    exceptionLogger.error ("unrecoverable error", it)

                    net.mail.send(
                            subject = "reported error",
                            message = it.stackTraceAsString(),
                            destination = "vachagan.balayan@gmail.com",
                            senderName = "Jarvis",
                            senderAddress = "jarvis@everywhere.com"
                    )
                }
            }
            else -> wtf()
        }
    }

    fun property(key: String): String {
        return executeAndGetMandatory {
            System.getProperty(key).apply { condition(notNullOrEmpty(this)) }
        }
    }

    fun optionalProperty(key: String): Option<String> {
        return executeAndGetSilent {
            System.getProperty(key).apply { condition(notNullOrEmpty(this)) }
        }
    }

    fun ensurePropertiesAreProvided(vararg keys: String) {
        keys.forEach {
            mandatoryCondition(notNullOrEmpty(System.getProperty(it)), "system property '$it' is required")
        }
    }

    fun time(): Long {
        return System.currentTimeMillis()
    }

    fun random(): Double {
        return ThreadLocalRandom.current().nextDouble()
    }

    fun exit() {
        System.exit(0);
    }

}