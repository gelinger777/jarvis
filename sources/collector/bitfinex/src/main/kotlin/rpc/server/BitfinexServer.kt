package rpc.server

import global.addShutdownHook
import global.logger
import io.grpc.ServerBuilder
import proto.CollectorGrpc
import util.cpu

class BitfinexServer(val port: Int, val config: Config) {
    val log by logger()

    private val server = ServerBuilder
            .forPort(port)
            .addService(CollectorGrpc.bindService(BitfinexService(config)))
            .executor(cpu.executors.io)
            .build()

    init {
        addShutdownHook {
            log.info("shutting down bitfinex server")
            server.shutdown()
        }

        // start server for the service
        server.start()
    }
}