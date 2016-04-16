package com.tars.util.net;

import com.tars.util.net.http.HttpHub;
import com.tars.util.net.socket.SocketHub;
import com.tars.util.net.ws.WebsocketHub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tars.util.exceptions.ExceptionUtils.notImplemented;
import static com.tars.util.exceptions.ExceptionUtils.onUnrecoverableFailure;
import static com.tars.util.validation.Validator.condition;

/**
 * Instantiates essential components for communication over network.
 */
@Deprecated
public class NetworkUtils {

  private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);

  private static SocketHub socketHub;
  private static HttpHub httpHub;
  private static WebsocketHub wsHub;

  private static volatile boolean isInitialized;

  // lifecycle

  public static void init() {
    socketHub = new SocketHub();
    httpHub = new HttpHub();
    wsHub = new WebsocketHub();

    onUnrecoverableFailure(throwable -> close());
    isInitialized = true;
    log.info("initialized");
  }

  public static void close() {
    socketHub.release();
    httpHub.release();
    isInitialized = false;
    log.info("closed");
  }

  // interface

  public static SocketHub socket() {
    condition(isInitialized, "network utils are not initialized");
    return socketHub;
  }

  public static HttpHub http() {
    condition(isInitialized, "network utils are not initialized");
    return httpHub;
  }

  public static WebsocketHub websocket() {
    condition(isInitialized, "network utils are not initialized");
    return wsHub;
  }

  public static Object wamp() {
    condition(isInitialized, "network utils are not initialized");
    return notImplemented();
  }

}
