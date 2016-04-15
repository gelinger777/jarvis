package rpc.server

import extensions.logger
import io.grpc.ServerBuilder
import proto.CollectorGrpc
import util.cpu

class BitfinexServer(val port: Int) {
    val log by logger()

    private val server = ServerBuilder
            .forPort(port)
            .addService(CollectorGrpc.bindService(CollectorServer()))
            .executor(cpu.executors.io)
            .build()

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                log.info("shutting down bitfinex server")
                server.shutdown()
            }
        })

        server.start()
    }
}