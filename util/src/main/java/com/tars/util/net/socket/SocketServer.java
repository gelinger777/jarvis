package com.tars.util.net.socket;

import rx.Observable;

/**
 * Represents socket server.
 * Server is not reusable, once stopped it shall be recycled.
 */
public interface SocketServer {

  /**
   * Initiate start of this server. Appropriate address will be allocated, and server will be
   * accessible for clients to initiate connection.
   */
  SocketServer start();

  /**
   * Initiate stop of server to stop and release all current connections.
   */
  SocketServer stop();

  /**
   * Stream of newly connected clients.
   *
   * Note : by default observers of this stream will execute on event loop thread, use observeOn and
   * pass appropriate scheduler depending on your observers task.
   */
  Observable<SocketClient> stream();

  /**
   * Checks if server is up and running normally.
   */
  boolean isAlive();
}



