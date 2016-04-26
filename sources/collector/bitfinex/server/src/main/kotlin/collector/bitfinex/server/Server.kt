package collector.bitfinex.server

import global.logger
import io.grpc.ServerBuilder
import io.grpc.ServerServiceDefinition
import util.cleanupTasks
import util.cpu

internal class Server(val port: Int, val service: ServerServiceDefinition) {
    val log by logger()

    private val server = ServerBuilder
            .forPort(port)
            .addService(service)
            .executor(cpu.executors.io)
            .build()

    fun start() {
        log.info("starting a ${service.name} server on $port")

        server.start()
        cleanupTasks.add("server:$port", { server.shutdown() })
    }

    fun stop() {
        log.info("shutting down ${service.name} server")

        cleanupTasks.remove("server:$port")
        server.shutdown()
    }
}