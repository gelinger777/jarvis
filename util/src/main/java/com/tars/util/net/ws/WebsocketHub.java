package com.tars.util.net.ws;

public final class WebsocketHub {

  public synchronized WebsocketServer server(int port, String path) {
    return new Server(port, path);
  }

  public synchronized WebsocketClient client(String address) {
    return new IndependentClient(address);
  }
}
