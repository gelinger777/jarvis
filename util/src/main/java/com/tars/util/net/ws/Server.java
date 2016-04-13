package com.tars.util.net.ws;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.function.Supplier;

import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.tars.util.exceptions.ExceptionUtils.wtf;

class Server implements WebsocketServer {

  private static final Logger log = LoggerFactory.getLogger(WebsocketServer.class);

  final org.eclipse.jetty.server.Server server;

  final LinkedList<WebsocketClient> clients = new LinkedList<>();
  final PublishSubject<WebsocketClient> clientStream = PublishSubject.create();

  public Server(int port, String path) {
    server = new org.eclipse.jetty.server.Server();
    ServerConnector connector = new ServerConnector(server);
    server.addConnector(connector);

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);

    connector.setPort(port);

    // create endpoint that will delegate all lifecycle invocations to this instance
    Supplier<AcceptedClient> supplier = () -> new AcceptedClient(this);

    // server endpoint configuration that will create delegating endpoints for each client
    ServerEndpointConfig config = ServerEndpointConfig.Builder
        .create(AcceptedClient.class, path)
        .configurator(new Configurator() {
          @Override
          @SuppressWarnings("unchecked")
          public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            return (T) supplier.get();
          }
        })
        .build();

    try {
      WebSocketServerContainerInitializer
          .configureContext(context)
          .addEndpoint(config);
    } catch (Exception cause) {
      clientStream.onError(cause);
    }
    log.debug("created");
  }

  @Override
  public WebsocketServer start() {
    log.info("starting");
    try {
      server.start();
    } catch (Exception cause) {
      clientStream.onError(cause);
    }
    return this;
  }

  @Override
  public WebsocketServer stop() {
    try {
      log.debug("stopping clients");
      while (!clients.isEmpty()) {
        clients.poll().stop();
      }

      log.debug("stopping server");
      server.stop();
    } catch (Exception cause) {
      wtf(cause);
    }

    return this;
  }

  @Override
  public Observable<WebsocketClient> stream() {
    return clientStream;
  }

  @Override
  public boolean isAlive() {
    return server.isStarted() && !clientStream.hasCompleted();
  }

}
