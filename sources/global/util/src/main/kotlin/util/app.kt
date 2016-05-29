package util

import rx.Observable
import rx.subjects.PublishSubject
import util.global.*

object app {
    val log by logger("app")
    val exceptionLogger by logger("exc")
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
                    log.info("REPORTED ERROR", it)
                }
                onUnrecoverableFailure {
                    log.info("UNRECOVERABLE ERROR", it)
                }
            }
            "prod" -> {
                onReportedFailure {
                    log.info("REPORTED ERROR", it)

                    net.mail.send(
                            subject = "reported error",
                            message = it.stackTraceAsString()
                    )
                }

                onUnrecoverableFailure {
                    log.info("UNRECOVERABLE ERROR", it)

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

    fun time() : Long {
        return System.currentTimeMillis()
    }

}