package util.misc;


/**
 * Trigger is a simple class that holds two closures, and will run them upon request.
 */
class Trigger(
        private val on: () -> Unit,
        private val off: () -> Unit) {

    fun on() {
        on.invoke()
    }

    fun off() {
        off.invoke()
    }
}