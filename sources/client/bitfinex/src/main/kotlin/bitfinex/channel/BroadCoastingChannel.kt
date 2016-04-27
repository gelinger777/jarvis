package collector.bitfinex.server.channel

import com.google.gson.JsonArray
import rx.Observable
import rx.subjects.PublishSubject

internal abstract class BroadCoastingChannel<T>(
        var name: String,
        val subject: PublishSubject<T> = PublishSubject.create<T>(),
        val observable: Observable<T> = subject.asObservable()
) {

    abstract fun parse(array: JsonArray)

    fun complete() {
        subject.onCompleted()
    }
}
