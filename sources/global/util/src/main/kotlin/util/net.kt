package util

import com.tars.util.net.http.HttpHub
import com.tars.util.net.pusher.PusherHub
import com.tars.util.net.socket.SocketHub
import com.tars.util.net.ws.WebsocketClient
import com.tars.util.net.ws.WebsocketHub
import com.tars.util.net.ws.WebsocketServer
import io.grpc.ServerServiceDefinition
import util.grpc.GrpcServer

object net {
    val socket by lazy {
        val socketHub = SocketHub()
        cleanupTasks.internalAdd({ socketHub.release() }, 1, "socket-hub")
        socketHub
    }
    val http by lazy {
        val httpHub = HttpHub()
        cleanupTasks.internalAdd({ httpHub.release() }, 1, "http-hub")
        httpHub
    }

    // todo add cleanup tasks
    // todo move mailer here
    // todo move grpc stuff here

    fun wsServer(port: Int, path: String): WebsocketServer {
        val server = WebsocketHub.server(port, path)
        cleanupTasks.internalAdd({ server.stop() }, 1)
        return server
    }

    fun wsClient(address: String): WebsocketClient {
        val client = WebsocketHub.client(address)
        cleanupTasks.internalAdd({ client.stop() }, 1)
        return client
    }

    fun grpcServer(port: Int, service: ServerServiceDefinition): GrpcServer {
        return GrpcServer(port, service)
    }

    val pusher by lazy {
        PusherHub()
    }

}