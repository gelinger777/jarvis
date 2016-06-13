package util.misc

import util.global.areSame
import util.global.condition
import util.global.getMandatory

class RefCountHolder<T>(val supplier: () -> T, val finalizer: (T) -> Unit) {

    private val toggle: RefCountToggle
    private var instance: T? = null

    init {
        toggle = RefCountToggle(
                { instance = supplier.invoke() },
                {
                    finalizer.invoke(getMandatory(instance))
                    instance = null
                }
        )
    }

    fun requestInstance(): T {
        toggle.increment()
        return getMandatory(instance)
    }

    fun returnInstance(instance: T?) {
        condition(areSame(this.instance, instance))
        toggle.decrement()
    }
}
