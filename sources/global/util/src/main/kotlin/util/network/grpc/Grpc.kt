package util.network.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.ServerServiceDefinition
import util.maid
import util.cpu
import util.global.executeAndGetMandatory

class Grpc {

    fun server(port: Int, service: ServerServiceDefinition): GrpcServer {
        return GrpcServer(port, service)
    }

    fun channel(host: String, port: Int): ManagedChannel {
        val channel = executeAndGetMandatory {
            ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext(true)
                    .executor(cpu.executors.io)
                    .build()
        }
        maid.add(
                task = { channel.shutdown() },
                priority = 1,
                key = "grpc|$host|$port"
        )
        return channel
    }

}