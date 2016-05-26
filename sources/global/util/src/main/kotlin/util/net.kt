package util

import com.tars.util.net.socket.SocketHub
import util.global.notImplemented
import util.network.grpc.Grpc
import util.network.http.HttpHub
import util.network.mail.Postman
import util.network.pusher.PusherHub
import util.network.websocket.WebsocketHub

object net {
    val socket by lazy { notImplemented<SocketHub>() }

    val http by lazy { HttpHub() }

    val ws by lazy { WebsocketHub() }

    val grpc by lazy { Grpc() }

    val pusher by lazy { PusherHub() }

    val mail by lazy { Postman() }

}