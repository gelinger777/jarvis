package util

import rx.Observable
import rx.subjects.PublishSubject
import util.global.*
import java.util.concurrent.ThreadLocalRandom

object app {
    val log = logger("app")
    val exceptionLogger = logger("exc")
    val profile: String

    internal val reportedErrors = PublishSubject.create<Throwable>()
    internal val unrecoverableErrors = PublishSubject.create<Throwable>()

    init {
        val logs = System.getProperty("logPath")
        profile = System.getProperty("profile")

        mandatoryCondition(notNullOrEmpty(logs), "'logPath' system property must be specified")
        mandatoryCondition(notNullOrEmpty(profile), "'profile' system property must be specified")

        log.debug { "profile : $profile" }
        log.debug { "log root : $logs" }

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


                reportedErrors.forEach {
                    exceptionLogger.warn ("unexpected error", it)

                    net.mail.send(
                            subject = "reported error",
                            message = it.stackTraceAsString()
                    )
                }

                unrecoverableErrors.forEach {
                    exceptionLogger.error ("unrecoverable error", it)

                    net.mail.send(
                            subject = "unrecoverable error",
                            message = it.stackTraceAsString()
                    )
                }
            }
            else -> wtf("unknown profile : $profile")

        }
    }

    fun isDevProfile(): Boolean {
        return profile == "dev"
    }

    fun reportedErrors(): Observable<Throwable> {
        return reportedErrors;
    }

    fun unrecoverableErrors(): Observable<Throwable> {
        return unrecoverableErrors;
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

    fun prop(key: String): String {
        return executeAndGetMandatory { System.getProperty(key) }
    }
}