package util.network.pusher

import com.pusher.client.Pusher
import rx.Observable
import rx.subjects.PublishSubject
import util.cleanupTasks
import util.cpu
import util.global.logger

class PusherHub {

    private val log by logger("pusher")

    fun stream(pusherKey: String, channelKey: String, eventType: String): Observable<String> {
        val pusher = Pusher(pusherKey)
        pusher.connect()

        cleanupTasks.internalAdd(
                task = { pusher.disconnect() },
                priority = 1,
                key = "pusher|$pusherKey|$channelKey|$eventType"
        )

        val channel = pusher.subscribe(channelKey)

        val subject = PublishSubject.create<String>()

        channel.bind(eventType) { ch, ev, data ->
            log.debug("{} : {} : {}", ch, ev, data)
            subject.onNext(data)
        }

        return subject.observeOn(cpu.schedulers.io).onBackpressureBuffer()
    }
}
