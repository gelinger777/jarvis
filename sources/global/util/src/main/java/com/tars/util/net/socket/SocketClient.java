package com.tars.util.net.socket;

import rx.Observable;

/**
 * Represents a single socket client.
 * Client is not reusable, once stopped it shall be recycled.
 */
public interface SocketClient {
  /**
   * Initiate connection for this client and start accepting messages.
   */
  SocketClient start();

  /**
   * Close connection and complete the client stream.
   */
  SocketClient stop();

  /**
   * Send message to the other connected endpoint. If client is not currently connected (not alive)
   * then messages will be stored in temporary buffer and will be flushed as soon as client gets
   * accepted.
   */
  SocketClient send(byte[] message);

  /**
   * Stream of incoming messages.
   *
   * Note : by default observers of this stream will execute on event loop thread, use observeOn and
   * pass appropriate scheduler depending on your observers task.
   */
  Observable<byte[]> stream();

  /**
   * Checks if client is up and running normally.
   */
  boolean isAlive();
}
