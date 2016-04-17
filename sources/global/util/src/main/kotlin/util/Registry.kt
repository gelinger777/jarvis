package eventstore

import com.tars.util.common.RefCountHolder
import global.computeIfAbsent
import global.getOptional

internal class Registry<K, V>(
        val supplierFactory: (K) -> (() -> V),
        val finalizer: (V) -> Unit
) {

    val registry = mutableMapOf<K, RefCountHolder<V>>()

    fun requestInstance(key: K): V {
        // create holder if not present
        val holder = registry.computeIfAbsent(key, {
            RefCountHolder(supplierFactory.invoke(it), finalizer)
        })

        // request an instance
        return holder.requestInstance()
    }

    fun returnInstance(key: K, value: V) {
        registry.getOptional(key)
                .ifPresent { it.returnInstance(value) }
                .ifNotPresent { throw IllegalStateException("no holder was found for key " + key) }
    }

}