package util.misc

import javax.annotation.concurrent.ThreadSafe

/**
 * RefCountToggle is extending Trigger with reference counting functionality,
 * on closure will be executed the very first time reference counter increments from 0,
 * and off closure will execute only when positive counter decrements to 0.
 */
@ThreadSafe
class RefCountToggle(
        private val on: () -> Unit,
        private val off: () -> Unit) {


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

    @Synchronized fun forceOn() {
        refCount = 1;
        on.invoke()
    }

    @Synchronized fun forceOff() {
        refCount = 0;
        off.invoke()
    }
}
