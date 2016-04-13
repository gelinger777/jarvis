package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;

public class Server {

  final Logger logger = LoggerFactory.getLogger(Server.class);
  io.grpc.Server server;

  public Server(int port, ServerServiceDefinition service) {
    server = ServerBuilder
        .forPort(port)
        .addService(service)
        .build();
  }

  public void start() throws IOException {
    server.start();
    logger.info("Server started");
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        Server.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  public void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  public void blockUntilShutdown() {
    if (server != null) {
      try {
        server.awaitTermination();
      } catch (InterruptedException ignored) {
      }
    }
  }

}
