package util.misc

class RefCountHolder<T>(supplier: () -> T, finalizer: (T) -> Unit) {

    private val toggle: RefCountToggle
    private var instance: T? = null

    init {
        toggle = RefCountToggle(
                { instance = supplier.invoke() },
                {
                    finalizer.invoke(this.instance ?: throw IllegalStateException("instance was not created"))
                    instance = null
                }
        )
    }

    fun requestInstance(): T {
        toggle.increment()
        return this.instance ?: throw IllegalStateException("instance was not created")
    }

    fun returnInstance(instance: T?) {
        if (instance == null) {
            throw NullPointerException()
        }

        if (instance !== this.instance) {
            throw IllegalStateException("resource does not belong to holder")
        }

        toggle.decrement()
    }
}
