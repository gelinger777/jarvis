package util

import util.global.wtf
import java.util.function.Consumer

@Suppress("UNCHECKED_CAST")
class Option<T>(private val value: T?) {

    fun mutable(): MutableOption<T> {
        return MutableOption.ofNullable(value)
    }

    fun get(): T {
        return value ?: throw IllegalStateException("option is empty")
    }

    fun isPresent(): Boolean {
        return value != null
    }

    fun isNotPresent(): Boolean {
        return value == null
    }

    fun filter(predicate: (T) -> Boolean): Option<T> {
        if (value != null && predicate.invoke(value)) {
            return this
        }
        return empty()
    }

    fun <U> map(mapper: (T) -> U): Option<U> {
        if (value != null) {
            return Option(mapper.invoke(value))
        }

        return empty()
    }

    fun <U> flatMap(mapper: (T) -> Option<U>): Option<U> {
        if (value != null) {
            return mapper.invoke(value)
        }

        return empty()
    }

    fun ifNotPresentTake(other: T): Option<T> {
        if (value == null) {
            return Option(other)
        }

        return this
    }

    fun ifNotPresentCompute(supplier: () -> T): Option<T> {
        if (value == null) {
            return Option(supplier.invoke())
        }

        return this
    }

    /**
     * Accept value regardless of current state.
     */
    fun <U> take(other: U): Option<U> {
        return Option(other)
    }

    fun <U> take(other: Option<U>): Option<U> {
        return other
    }

    /**
     * Clear the value (same as empty optional).
     */
    fun <U> clear(): Option<U> {
        return empty()
    }

    fun ifPresent(consumer: Consumer<T>): Option<T> {
        if (value != null) {
            consumer.accept(value);
        }
        return this;
    }

    fun ifPresent(action: (T) -> Unit): Option<T> {
        if (value != null) {
            action.invoke(value)
        }
        return this
    }

    fun ifNotPresent(runnable: Runnable): Option<T> {
        if (value == null) {
            runnable.run()
        }
        return this;
    }

    fun ifNotPresent(action: () -> Unit): Option<T> {
        if (value == null) {
            action.invoke()
        }
        return this;
    }

    /**
     * Throw exception if value is not present.
     */
    fun <X : Throwable> ifNotPresentThrow(exceptionSupplier: () -> X): Option<T> {
        if (value == null) {
            throw exceptionSupplier.invoke()
        }
        return this
    }

    fun ifNotPresentWTF(): Option<T> {
        if (value == null) {
            wtf("this should not happen")
        }
        return this
    }

    // equals, hashcode and toString (these are delegated to actual value)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Option<*>) {
            return false
        }

        return value == other.value
    }

    override fun hashCode(): Int {
        return if (value != null) value.hashCode() else 0
    }

    override fun toString(): String {
        return if (value != null) String.format("Option[%s]", value)
        else "Option.empty"
    }

    companion object {
        val empty = Option<Any>(null)

        @JvmStatic fun <T> empty(): Option<T> {
            @Suppress("CAST_NEVER_SUCCEEDS")
            return empty as Option<T>
        }

        @JvmStatic fun <T> of(value: T?): Option<T> {
            if (value == null) {
                throw NullPointerException()
            }
            return Option(value)
        }

        @JvmStatic fun <T> ofNullable(value: T?): Option<T> {
            return Option(value)
        }

    }
}
