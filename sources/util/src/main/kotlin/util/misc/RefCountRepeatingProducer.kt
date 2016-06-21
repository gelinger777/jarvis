package util.misc

import rx.Observable
import rx.subjects.PublishSubject
import util.global.executeAndGetSilent
import java.util.concurrent.TimeUnit

class RefCountRepeatingProducer<T>(
        val name: String,
        val producer: () -> T,
        @Volatile var delay: Long,
        val terminationTimeout: Long = 10000) {

    private val observable = PublishSubject.create<T>()

    private val task = RefCountRepeatingTask(
            name,
            { executeAndGetSilent(producer).ifPresent { observable.onNext(it) } },
            delay,
            terminationTimeout
    )

    fun increment() {
        task.increment()
    }

    fun decrement() {
        task.decrement()
    }

    fun forceStart() {
        task.forceStart()
    }

    fun forceStop() {
        task.forceStop()
    }

    fun isStarted(): Boolean {
        return task.isStarted()
    }

    fun isAlive(): Boolean {
        return task.isAlive()
    }

    fun isNotAlive(): Boolean {
        return task.isNotAlive()
    }

    fun isCurrentThread(): Boolean {
        return task.isCurrentThread()
    }

    fun stream(): Observable<T> {
        return observable
    }

}