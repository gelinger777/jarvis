//package util.chronicle
//
//import rx.Observable
//import rx.subjects.PublishSubject
//import util.misc.RefCountToggle
//import util.cpu
//
//
///**
// * All Watchers share an event loop to check if a chronicles has new values,
// * no code is executed unless there is at least one subscriber,
// * if all subscribers unsubscribe watcher is no longer monitored.
// */
//internal class Watcher(val path: String) {
//    private val log by logger()
//    private val subject = PublishSubject.create<ByteArray>()
//    private val chronicle = eventstore.server.storage.chronicle(path)
//    private var tailer = chronicle.createTailer()
//
//    val toggle = RefCountToggle({
//        log.debug("client subscribed")
//
//        // move pointer to latest value
//        tailer.toEnd()
//
//        // add this watcher to event loop
//        watchersEventLoop.add(this)
//    }, {
//        log.debug("client unsubscribed")
//
//        // remove this watcher from event loop
//        watchersEventLoop.remove(this)
//    })
//
//    val observable = subject
//            .doOnSubscribe({ toggle.increment() })
//            .doOnUnsubscribe({ toggle.decrement() })
//            .subscribeOn(cpu.schedulers.io)
//            .observeOn(cpu.schedulers.io)
//            .unsubscribeOn(cpu.schedulers.io)
//
//    /**
//     * Returns an Observable that when subscribed will stream any new data for the underlying queue.
//     */
//    fun stream(): Observable<ByteArray> {
//        return observable
//    }
//
//    fun currentIndex(): Long {
//        return tailer.index()
//    }
//
//
//    @Synchronized fun checkAndEmit(): Boolean {
//        if (subject.hasCompleted()) return false
//
//        try {
//            // if new data is available
//            if (tailer.nextIndex()) {
//
//                // read data from watcher
//                log.trace("extracting")
//                val bytes = eventstore.server.readFrameAsByteArray(tailer)
//
//                // emit data
//                log.trace("publishing")
//                subject.onNext(bytes)
//            }
//            return true
//        } catch (cause: Exception) {
//            log.error("unexpected exception", cause)
//            subject.onError(cause)
//            return false
//        }
//    }
//
//    fun close() {
//        // release all resources and remove from eventLoop
//        toggle.reset()
//
//        // send completion to all subscribers
//        subject.onCompleted()
//    }
//}