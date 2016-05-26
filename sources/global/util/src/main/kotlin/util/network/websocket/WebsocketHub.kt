package util.network.websocket

import com.tars.util.net.ws.WebsocketClient
import com.tars.util.net.ws.WebsocketHub
import com.tars.util.net.ws.WebsocketServer
import util.cleanupTasks

class WebsocketHub {
    fun server(port: Int, path: String): WebsocketServer {
        return WebsocketHub.server(port, path)
                .apply { cleanupTasks.internalAdd({ this.stop() }, 1) }
    }

    fun client(address: String): WebsocketClient {
        return WebsocketHub.client(address)
                .apply { cleanupTasks.internalAdd({ this.stop() }, 1) }
    }
}