package util

@Suppress("UNCHECKED_CAST")
class Option<T>(val value: T?) {

    // interface

    fun get(): T {
        return value ?: throw IllegalStateException("option is empty")
    }

    fun isPresent(): Boolean {
        return value != null
    }

    fun filter(predicate: (T) -> Boolean): Option<T> {
        if (value != null && !predicate.invoke(value)) {
            return empty()
        }
        return this
    }

    fun <U> map(mapper: (T) -> U): Option<U> {
        if (value != null) {
            return Option(mapper.invoke(value))
        }

        return this as Option<U>
    }

    fun <U> flatMap(mapper: (T) -> Option<U>): Option<U> {
        if (value != null) {
            return mapper.invoke(value)
        }

        return this as Option<U>
    }

    fun ifNotPresentTake(other: T): Option<T> {
        if (value == null) {
            return Option(other)
        }

        return this
    }

    fun ifNotPresentTakeCompute(supplier: () -> T): Option<T> {
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

    fun ifPresent(action: (T) -> Unit): Option<T> {
        if (value != null) {
            action.invoke(value)
        }
        return this
    }

    fun ifNotPresent(action: () -> Unit): Option<T> {
        if (value == null) {
            action.invoke()
        }
        return this;
    }

    //    /**
    //     * Throw exception if value is not present.
    //     */
    //    fun <X : Throwable> ifNotPresentThrow(exceptionSupplier: Supplier<out X>): Option<T> {
    //        if (value == null) {
    //            throw exceptionSupplier.get()
    //        }
    //        return this
    //    }
    //
    //    /**
    //     * If not present execute action.
    //     */
    //    fun ifNotPresent(action: Runnable): Option<T> {
    //        if (value == null) {
    //            action.run()
    //        }
    //        return this
    //    }
    //
    //    fun anyExceptionThrow(): Option<T> {
    //        return notImplemented()
    //    }
    //
    //    fun anyExceptionHandle(handler: Consumer<Throwable>): Option<T> {
    //        return notImplemented()
    //    }
    //
    // equals, hashcode and toString (these are delegated to actual value)

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }

        if (obj !is Option<*>) {
            return false
        }

        return value == obj.value
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

        fun <T> empty(): Option<T> {
            return empty as Option<T>
        }

        fun <T> of(value: T): Option<T> {
            return Option(value)
        }

        fun <T> ofNullable(value: T?): Option<T> {
            return Option(value)
        }
    }
}
