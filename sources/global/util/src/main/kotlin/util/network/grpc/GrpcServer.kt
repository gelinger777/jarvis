package util.network.grpc

import io.grpc.ServerBuilder
import io.grpc.ServerServiceDefinition
import util.cleanupTasks
import util.cpu
import util.global.logger

class GrpcServer(val port: Int, val service: ServerServiceDefinition) {
    val log = logger("GrpcServer")

    private val server = ServerBuilder
            .forPort(port)
            .addService(service)
            .executor(cpu.executors.io)
            .build()

    fun start(): GrpcServer {
        log.info { "starting a ${service.name} server on $port" }

        server.start()
        cleanupTasks.internalAdd(
                task = { server.shutdown() },
                priority = 1,
                key = "server:$port"
        )
        return this
    }

    fun stop() {
        log.info { "shutting down ${service.name} server" }

        cleanupTasks.remove("server:$port")
        server.shutdown()
    }

//    fun blockForTermination() {
//        log.info{"awaiting for termination of ${service.name}"}
//        if (server != null) {
//            try {
//                server.awaitTermination()
//            } catch (ignored: InterruptedException) {
//            }
//        }
//    }
}