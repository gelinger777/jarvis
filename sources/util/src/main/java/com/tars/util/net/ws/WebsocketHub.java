package com.tars.util.net.ws;

public final class WebsocketHub {

  public static WebsocketServer server(int port, String path) {
    return new Server(port, path);
  }

  public static WebsocketClient client(String address) {
    return new IndependentClient(address);
  }
}
