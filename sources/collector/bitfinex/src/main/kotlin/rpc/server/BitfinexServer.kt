package rpc.server

import global.logger
import io.grpc.ServerBuilder
import proto.CollectorGrpc
import util.cleanupTasks
import util.cpu

class BitfinexServer(val port: Int, val config: Config) {
    val log by logger()

    private val server = ServerBuilder
            .forPort(port)
            .addService(CollectorGrpc.bindService(BitfinexService(config)))
            .executor(cpu.executors.io)
            .build()

    init {
        log.info("init")
        cleanupTasks.add({
            log.info("shutdown")
            server.shutdown()
        })

        // start server for the service
        server.start()
    }
}