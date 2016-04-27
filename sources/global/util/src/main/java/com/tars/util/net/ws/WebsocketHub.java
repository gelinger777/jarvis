package com.tars.util.net.ws;

import org.slf4j.*;

import java.util.*;

import static com.tars.util.exceptions.ExceptionUtils.*;

public final class WebsocketHub {

  private static final Logger log = LoggerFactory.getLogger("websocket");

  // servers (have their own connected clients)
  private Map<String, WebsocketServer> servers = new HashMap<>();

  // independent clients
  private Map<String, WebsocketClient> clients = new HashMap<>();

  public WebsocketHub() {
    log.info("init");
  }

  public synchronized WebsocketServer server(int port, String path) {
    return servers.computeIfAbsent(port + ":" + path, key -> new Server(port, path));
  }

  public synchronized WebsocketClient client(String address) {
    return clients.computeIfAbsent(address, IndependentClient::new);
  }

  public void release() {
    log.debug("shutdown");
    if (!clients.isEmpty()) {
      log.debug("shutting down all websocket clients");
      clients.values().forEach(client -> executeSilent(client::stop));
    }
    if (!servers.isEmpty()) {
      log.debug("shutting down all websocket servers");
      servers.values().forEach(server -> executeSilent(server::stop));
    }
  }
}
