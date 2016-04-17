package server;

//public class ServiceImpl implements Service {
//
//  @Override
//  public void sendSingleGetSingle(Message request, StreamObserver<Message> responseObserver) {
//    responseObserver.onNext(Message.newBuilder().setText("response").build());
//    responseObserver.onCompleted();
//  }
//
//  @Override
//  public void sendSingleGetStream(Message request, StreamObserver<Message> responseObserver) {
//    responseObserver.onNext(Message.newBuilder().setText("response-1").build());
//    responseObserver.onNext(Message.newBuilder().setText("response-2").build());
//    responseObserver.onNext(Message.newBuilder().setText("response-3").build());
//    responseObserver.onCompleted();
//  }
//
//  @Override
//  public StreamObserver<Message> sendStreamGetSingle(StreamObserver<Message> responseObserver) {
//    return new StreamObserver<Message>() {
//      int count = 0;
//
//      @Override
//      public void onNext(Message message) {
//        count++;
//      }
//
//      @Override
//      public void onError(Throwable throwable) {
//
//      }
//
//      @Override
//      public void onCompleted() {
//        responseObserver.onNext(Message.newBuilder().setText("response for " + count + " requests").build());
//        responseObserver.onCompleted();
//      }
//    };
//  }
//
//  @Override
//  public StreamObserver<Message> sendStreamGetStream(StreamObserver<Message> responseObserver) {
//    return new StreamObserver<Message>() {
//      int count = 0;
//
//      @Override
//      public void onNext(Message message) {
//        count++;
//        responseObserver.onNext(Message.newBuilder().setText("response " + count + " " + message.getText()).build());
//      }
//
//      @Override
//      public void onError(Throwable throwable) {
//
//      }
//
//      @Override
//      public void onCompleted() {
//        responseObserver.onCompleted();
//      }
//    };
//  }
//
//  public static void main(String[] args) throws IOException {
//    Server server = new Server(8980, ServiceGrpc.bindService(new ServiceImpl()));
//    server.start();
//    server.blockUntilShutdown();
//  }
//}
