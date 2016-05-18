package util

import com.tars.util.net.http.HttpHub
import com.tars.util.net.socket.SocketHub
import com.tars.util.net.ws.WebsocketHub
import util.global.logger

object net {
    private val log by logger()

    val socket by lazy {
        val socketHub = SocketHub()
        cleanupTasks.internalAdd("socket-hub", { socketHub.release() }, 1)
        socketHub
    }
    val http by lazy {
        val httpHub = HttpHub()
        cleanupTasks.internalAdd("http-hub", { httpHub.release() }, 1)
        httpHub
    }
    val websocket by lazy {
        val websocketHub = WebsocketHub()
        cleanupTasks.internalAdd("websocket-hub", { websocketHub.release() }, 1)
        websocketHub
    }

    // todo : move mailer here
    // todo : move grpc methods here
}