package com.tars.util.net.ws;

import rx.Observable;

public interface WebsocketServer {

  /**
   * Initiate start of this server. Appropriate address will be allocated, and server will be
   * accessible for clients to initiate connection.
   */
  WebsocketServer start();

  /**
   * Initiate stop of server to stop and release all current connections.
   */
  WebsocketServer stop();

  /**
   * Stream of newly connected clients.
   *
   * Note : by default observers of this stream will execute on event loop thread, use observeOn and
   * pass appropriate scheduler depending on your observers task.
   */
  Observable<WebsocketClient> stream();

  /**
   * Checks if server is up and running normally.
   */
  boolean isAlive();
}



