package util.global

// extension convenience functions

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import com.tars.util.misc.BatchPerSubscriber
import io.grpc.stub.StreamObserver
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rx.Observable
import rx.Scheduler
import rx.Subscriber
import rx.subjects.PublishSubject
import util.Option
import util.app
import util.cpu
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.function.Consumer


fun consoleStream(): Observable<String> {
    return Observable.create(object : Observable.OnSubscribe<String> {

        override fun call(subscriber: Subscriber<in String>) {
            if (subscriber.isUnsubscribed) {
                return;
            }

            val scanner = Scanner(System.`in`);
            var line = "";
            while (!subscriber.isUnsubscribed) {
                line = scanner.nextLine();

                if (line != "close") {
                    subscriber.onNext(line);
                } else {
                    break
                }
            }

            subscriber.onCompleted();
        }
    })
}

// observable

/**
 * Apply BatchPerSubscriber to observable.
 */
fun <T> Observable<T>.batch(scheduler: Scheduler = cpu.schedulers.io): Observable<Collection<T>> {
    return this.lift(BatchPerSubscriber<T>(scheduler))
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

fun logger(name: String): Logger {
    return LoggerFactory.getLogger(name)
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


fun <T : Message.Builder> T.readFromFS(propertyName : String): T {
    return this.apply {
        executeMandatory {

            app.log.info("getting location of configuration : $propertyName")
            val path = System.getProperty(propertyName)

            condition(notNullOrEmpty(path), "property was not provided")

            app.log.info("reading configuration from : $path")
            val json = FileUtils.readFileToString(File(path))

            app.log.info("merging configuration")
            JsonFormat.parser().merge(json, this)
        }
    }
}