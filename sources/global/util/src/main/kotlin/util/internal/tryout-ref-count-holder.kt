package util.internal

import util.app
import util.global.condition
import util.misc.RefCountHolder

/**
 * For resources that are expensive to allocate or need specific logic to deallocate.
 */
internal fun main(args: Array<String>) {
    val holder = RefCountHolder(
            {
                app.log.info { "providing instance" }
                "instance"
            },
            {
                app.log.info { "closing allocated resource : $it" }
            }
    )

    val instance1 = holder.requestInstance()
    val instance2 = holder.requestInstance()
    val instance3 = holder.requestInstance()

    condition(instance1 === instance2 && instance2 === instance3)

    holder.returnInstance(instance1)
    holder.returnInstance(instance2)
    holder.returnInstance(instance3)


}