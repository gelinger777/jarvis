package client;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import proto.CollectorGrpc;
import proto.CollectorGrpc.Collector;
import proto.CollectorGrpc.CollectorBlockingClient;
import util.cpu.executors;

public class Client {

  ManagedChannel channel;
  CollectorBlockingClient blockingStub;
  Collector asyncStub;

  /**
   * Construct client for accessing RouteGuide server at {@code host:port}.
   */
  public Client(String host, int port) {
    channel = ManagedChannelBuilder.forAddress(host, port)
        .executor(executors.INSTANCE.getIo()).usePlaintext(true).build();
    asyncStub = CollectorGrpc.newStub(channel);
    blockingStub = CollectorGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public static void main(String[] args) {
    Client client = new Client("localhost", 8980);

//    Message responseSingle = client.blockingStub.sendSingleGetSingle(message("barev"));
//    System.out.println(responseSingle);

//    client.asyncStub.sendSingleGetStream(Message.newBuilder().setText("barev").build(), printObserver());

//    StreamObserver<Message> requestObserver = client.asyncStub.sendStreamGetSingle(printObserver());
//    requestObserver.onNext(message("hi 1"));
//    requestObserver.onNext(message("hi 1"));
//    requestObserver.onNext(message("hi 1"));
//    requestObserver.onNext(message("hi 1"));
//    requestObserver.onCompleted();

//    StreamObserver<Message> requestStream = client.asyncStub.sendStreamGetStream(printObserver());
//    requestStream.onNext(message("barev"));
//    requestStream.onNext(message("vonces"));
//    requestStream.onNext(message("inch ka chka?"));
//    requestStream.onCompleted();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
