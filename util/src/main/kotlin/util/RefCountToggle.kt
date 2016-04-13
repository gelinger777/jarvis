package util

class RefCountToggle(private val start: () -> Unit, private val stop: () -> Unit) {

    private var refCount = 0

    @Synchronized fun increment() {
        if (refCount++ == 1) {
            start.invoke()
        }
    }

    @Synchronized fun decrement() {
        if (refCount-- == 0) {
            stop.invoke()
        }
    }
}
