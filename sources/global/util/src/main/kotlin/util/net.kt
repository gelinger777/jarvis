package util

import com.tars.util.exceptions.ExceptionUtils.onUnrecoverableFailure
import com.tars.util.net.http.HttpHub
import com.tars.util.net.socket.SocketHub
import com.tars.util.net.ws.WebsocketHub
import global.logger

object net {
    private val log by logger()

    val socket = SocketHub()
    val http = HttpHub()
    val websocket = WebsocketHub()

    // lifecycle

    fun init() {
        onUnrecoverableFailure { throwable -> close() }
        log.info("initialized")
    }

    fun close() {
        socket.release()
        http.release()
        log.info("closed")
    }

}