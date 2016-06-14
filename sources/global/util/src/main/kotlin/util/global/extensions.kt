package util.global

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import com.tars.util.misc.BatchOperator
import io.grpc.stub.StreamObserver
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import rx.Observable
import rx.Scheduler
import rx.Subscriber
import rx.subjects.PublishSubject
import util.Option
import util.app.log
import util.cpu
import util.logging.Logger
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.util.function.Consumer


// observable

/**
 * Apply BatchOperator to observable.
 */
fun <T> Observable<T>.batch(scheduler: Scheduler = cpu.schedulers.io): Observable<Collection<T>> {
    return this.lift(BatchOperator<T>(scheduler))
}

/**
 * Unpack batch.
 */
fun <T> Observable<out Collection<T>>.unpack(): Observable<T> {
    return this.lift(Observable.Operator { subscriber ->
        object : Subscriber<Collection<T>>() {
            override fun onCompleted() {
                subscriber.onCompleted()
            }

            override fun onError(e: Throwable) {
                subscriber.onError(e)
            }

            override fun onNext(ts: Collection<T>) {
                for (t in ts) {
                    if (!subscriber.isUnsubscribed) {
                        subscriber.onNext(t)
                    }
                }
            }
        }
    })
}

fun <T> Observable<Option<T>>.filterEmptyOptionals(): Observable<T> {
    return this.filter { it.isPresent() }.map { it.get() }
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

fun <T> Subscriber<T>.isSubscribed(): Boolean {
    return !this.isUnsubscribed
}

fun <T> StreamObserver<T>.subscribe(source: Observable<T>) {
    source.subscribe(
            { this.onNext(it) },
            { this.onError(it) },
            { this.onCompleted() }
    )
}

fun <T> StreamObserver<T>.complete(lastValue: T) {
    this.onNext(lastValue)
    this.onCompleted()
}

// logger

///**
// * Lazily get logger.
// */
//fun <R : Any> R.lazyLogger(): Lazy<Logger> {
//    return lazy { LoggerFactory.getLogger(this.javaClass.name) }
//}
//
///**
// * Lazily get logger.
// */
//fun <R : Any> R.lazyLogger(name: String): Lazy<Logger> {
//    return lazy { LoggerFactory.getLogger(name) }
//}

fun logger(name: String): Logger {
    return Logger(LogManager.getLogger(name))
}

// mutable map

/**
 * Get non null value or throw IllegalStateException.
 */
fun <K, V> Map<K, V>.getMandatory(key: K): V {
    return this[key] ?: whatever { wtf("nothing found for [$key]") }
}

fun <K, V> Map<K, V>.notContainsKey(key: K): Boolean {
    return !this.containsKey(key)
}

fun <K, V> MutableMap<K, V>.removeAndGetMandatory(key: K): V {
    return this.remove(key) ?: whatever { wtf("nothing found for [$key]") }
}

/**
 * Get optional value.
 */
fun <K, V> Map<K, V>.getOptional(key: K): Option<V> {
    return Option.ofNullable(this[key])
}

// todo remove after java 8 default method support

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

// Runnable and ()->Unit

fun Runnable.toClosure(): () -> Unit {
    return { this.run() }
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

// protobuf


fun <T : Message.Builder> T.readConfig(propertyName: String): T {
    return this.apply {
        executeMandatory {
            log.debug { "getting location of configuration : $propertyName" }
            val path = System.getProperty(propertyName)

            condition(notNullOrEmpty(path), "property was not provided")

            log.debug { "reading configuration from : $path" }
            val json = FileUtils.readFileToString(File(path), UTF_8)

            log.debug { "merging configuration" }
            JsonFormat.parser().merge(json, this)
        }
    }
}

/**
 * Check if number is in range [start, end)
 */
fun Long.inRange(start: Long, end: Long): Boolean {
    return this >= start && this < end
}

fun Thread.notInterrupted(): Boolean {
    return !this.isInterrupted
}

fun <T> Iterator<T>.optionalNext(): Option<T> {
    if (hasNext()) {
        return Option.of(next())
    } else {
        return Option.empty()
    }
}

fun <E> Collection<E>.notContains(element: E): Boolean {
    return !contains(element)
}