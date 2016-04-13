package rpc.client

import com.tars.util.exceptions.ExceptionUtils.notImplemented
import extensions.logger
import proto.CollectorGrpc

fun bitfinexClient(host: String, port: Int) : BitfinexClient {
    throw IllegalStateException()
}

class BitfinexClient{

}

enum class Service {
    BITFINEX
}

object services {
    val log by logger()

    fun blockingClient(service: Service, host: String = "localhost", port: Int = 8008): CollectorGrpc.CollectorBlockingClient {
        return notImplemented()
    }

    fun client(service: Service, host: String = "localhost", port: Int = 8008): CollectorGrpc.Collector {
        return notImplemented()
    }
}

fun main(args: Array<String>) {

//    val client = BitfinexClient("localhost", 50001)


}