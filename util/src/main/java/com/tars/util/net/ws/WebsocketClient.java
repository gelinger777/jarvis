package com.tars.util.net.ws;

import rx.Observable;

/**
 * Represents a single socket client.
 */
public interface WebsocketClient {
  /**
   * Initiate connection for this client and start accepting messages.
   */
  WebsocketClient start();

  /**
   * Close connection and complete the client stream.
   */
  WebsocketClient stop();

  /**
   * Send message to the other connected endpoint. If client is not currently connected (not alive)
   * then messages will be stored in temporary buffer and will be flushed as soon as client gets
   * accepted.
   */
  WebsocketClient send(String message);

  /**
   * Stream of incoming messages.
   *
   * Note : by default observers of this stream will execute on event loop thread, use observeOn and
   * pass appropriate scheduler depending on your observers task.
   */
  Observable<String> stream();

  /**
   * Checks if client is up and running normally.
   */
  boolean isAlive();
}
