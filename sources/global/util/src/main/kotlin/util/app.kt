package util

import rx.Observable
import rx.subjects.PublishSubject
import util.global.*

object app {
    val log by logger("application")
    val exceptionLogger by logger("exceptions")

    internal val unrecoverableErrors = PublishSubject.create<Throwable>()

    internal val reportedErrors = PublishSubject.create<Throwable>()

    init {
        val logsPath = System.getProperty("logsPath")
        val profile = System.getProperty("profile")

        mandatoryCondition(notNullOrEmpty(logsPath), "'logsPath' system property must be specified")
        mandatoryCondition(notNullOrEmpty(profile), "'profile' system property must be specified")

        log.info("started with profile : $profile")
        log.info("log files root : $logsPath")

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

                    mailer.send(
                            subject = "reported error",
                            message = it.stackTraceAsString()
                    )
                }

                onUnrecoverableFailure {
                    log.info("UNRECOVERABLE ERROR", it)

                    mailer.send(
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

    fun reportedErrors() : Observable<Throwable>{
        return reportedErrors;
    }

    fun unrecoverableErrors() : Observable<Throwable>{
        return unrecoverableErrors;
    }

}