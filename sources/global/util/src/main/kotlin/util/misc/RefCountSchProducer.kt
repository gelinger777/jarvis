package util.misc

import rx.Observable
import rx.subjects.PublishSubject
import util.cpu
import util.global.executeAndGetSilent
import util.global.notInterrupted

class RefCountSchProducer<T>(
        val name: String,
        val producer: () -> T,
        @Volatile var delay: Long,
        val terminationTimeout: Long = 10000) {

    private val observable = PublishSubject.create<T>()

    private val scheduledTask = {
        while (Thread.currentThread().notInterrupted()) {
            executeAndGetSilent(producer).ifPresent { observable.onNext(it) }
            cpu.sleep(delay)
        }
    }

    private val refCountTask = RefCountTask(name, scheduledTask, terminationTimeout)

    fun increment() {
        refCountTask.increment()
    }

    fun decrement() {
        refCountTask.decrement()
    }

    fun forceStart() {
        refCountTask.forceStart()
    }

    fun forceStop() {
        refCountTask.forceStop()
    }

    fun isStarted(): Boolean {
        return refCountTask.isStarted()
    }

    fun isAlive(): Boolean {
        return refCountTask.isAlive()
    }

    fun isNotAlive(): Boolean {
        return refCountTask.isNotAlive()
    }

    fun isCurrentThread(): Boolean {
        return refCountTask.isCurrentThread()
    }

    fun delay(delay: Long) {
        this.delay = delay
    }

    fun stream(): Observable<T> {
        return observable
    }

}