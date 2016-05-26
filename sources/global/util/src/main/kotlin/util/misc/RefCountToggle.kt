package util.misc

import javax.annotation.concurrent.ThreadSafe

@ThreadSafe
class RefCountToggle(private val on: () -> Unit, private val off: () -> Unit) {

    private var refCount = 0

    @Synchronized fun increment() {
        if (++refCount == 1) {
            on.invoke()
        }
    }

    @Synchronized fun decrement() {
        if (--refCount == 0) {
            off.invoke()
        }
    }

    @Synchronized fun reset() {
        if (refCount != 0) {
            refCount = 0;
            off.invoke()
        }
    }
}
