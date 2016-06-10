package util

import rx.Observable
import rx.subjects.PublishSubject
import util.global.*

object app {
    val log by lazyLogger("app")
    val exceptionLogger by lazyLogger("exc")
    val profile: String

    internal val unrecoverableErrors = PublishSubject.create<Throwable>()

    internal val reportedErrors = PublishSubject.create<Throwable>()

    init {
        val logs = System.getProperty("logPath")
        profile = System.getProperty("profile")

        mandatoryCondition(notNullOrEmpty(logs), "'logPath' system property must be specified")
        mandatoryCondition(notNullOrEmpty(profile), "'profile' system property must be specified")

        log.info("started with profile : $profile")
        log.info("log files root : $logs")

        when (profile) {
            "dev" -> {
                onReportedFailure {
                    exceptionLogger.info("REPORTED ERROR", it)
                }
                onUnrecoverableFailure {
                    exceptionLogger.info("UNRECOVERABLE ERROR", it)
                }
            }
            "prod" -> {
                onReportedFailure {
                    exceptionLogger.info("REPORTED ERROR", it)

                    net.mail.send(
                            subject = "reported error",
                            message = it.stackTraceAsString()
                    )
                }

                onUnrecoverableFailure {
                    exceptionLogger.info("UNRECOVERABLE ERROR", it)

                    net.mail.send(
                            subject = "unrecoverable error",
                            message = it.stackTraceAsString()
                    )
                }
            }
            else -> {
                wtf("unknown profile : $profile")
            }

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

    fun exit() {
        System.exit(0);
    }
}