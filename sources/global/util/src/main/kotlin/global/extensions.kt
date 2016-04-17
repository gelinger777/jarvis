package global

// extension convenience functions

import com.tars.util.misc.BatchPerSubscriber
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rx.Observable
import rx.subjects.PublishSubject
import util.Option
import util.cpu
import java.io.PrintWriter
import java.io.StringWriter
import java.util.function.Consumer


// observable

/**
 * Apply BatchPerSubscriber to observable.
 */
fun <T> Observable<T>.batch(): Observable<Collection<T>> {
    return this.lift(BatchPerSubscriber<T>(cpu.schedulers.io))
}

/**
 * Create GRPC compatible StreamObserver which will delegate all calls to this subject.
 */
fun <T> PublishSubject<T>.asGrpcObserver(): StreamObserver<T> {
    val subject = this
    return object : StreamObserver<T> {

        override fun onNext(value: T) {
            subject.onNext(value)
        }

        override fun onError(error: Throwable) {
            subject.onError(error)
        }

        override fun onCompleted() {
            subject.onCompleted()
        }
    }
}

// logger

/**
 * Lazily get logger.
 */
fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass.name) }
}

/**
 * Lazily get logger.
 */
fun <R : Any> R.logger(name: String): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(name) }
}

// mutable map

/**
 * Get non null value or throw IllegalStateException.
 */
fun <K, V> MutableMap<K, V>.getMandatory(key: K): V {
    return this[key] ?: throw IllegalStateException("nothing found for [$key]")
}

/**
 * Get optional value.
 */
fun <K, V> MutableMap<K, V>.getOptional(key: K): Option<V> {
    return Option.ofNullable(this[key])
}

/**
 * TEMPORARY : replacement for java 8 api
 */
fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, mappingFunction: (key: K) -> V): V {
    val v = this[key]
    if (v == null) {
        val newValue = mappingFunction.invoke(key)
        if (newValue != null) {
            put(key, newValue)
            return newValue
        } else {
            throw IllegalStateException("mapping function returned null for [$key]")
        }
    } else {
        return v
    }
}

/**
 * Both keys will point to the same object. One of the keys must be contained in the map.
 */
fun <K, V> MutableMap<K, V>.associateKeys(firstKey: K, secondKey: K) {
    val firstVal = this[firstKey]
    val secondVal = this[secondKey]

    if (firstVal != null && secondVal == null) {
        this.put(secondKey, firstVal);
        return
    }

    if (firstVal == null && secondVal != null) {
        this.put(firstKey, secondVal)
        return
    }

    throw IllegalStateException()
}

/**
 * Remove all entries that point to the same value that is mapped under provided key.
 */
fun <K, V> MutableMap<K, V>.removeWithAssociations(key: K) {
    val value = this[key] ?: throw IllegalStateException()
    val iterator = this.entries.iterator()

    for ((currentKey, currentValue) in iterator) {
        if (value === currentValue) {
            iterator.remove()
        }
    }
}

// throwable

fun Throwable.stackTraceAsString(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    this.printStackTrace(pw)
    return sw.toString()
}

// todo remove after java 8 default method support

fun <T> Consumer<T>.andThen(after: Consumer<T>): Consumer<T> {
    return object : Consumer<T> {
        override fun accept(t: T) {
            this.accept(t)
            after.accept(t)
        }
    }
}