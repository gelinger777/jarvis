package util

import com.tars.util.net.http.HttpHub
import com.tars.util.net.socket.SocketHub
import com.tars.util.net.ws.WebsocketHub
import global.logger

object net {
    private val log by logger()

    val socket by lazy {
        val socketHub = SocketHub()
        cleanupTasks.internalAdd({ socketHub.release() }, 1)
        socketHub
    }
    val http by lazy {
        val httpHub = HttpHub()
        cleanupTasks.internalAdd({ httpHub.release() }, 1)
        httpHub
    }
    val websocket by lazy {
        val websocketHub = WebsocketHub()
        cleanupTasks.internalAdd({ websocketHub.release() }, 1)
        websocketHub
    }
}