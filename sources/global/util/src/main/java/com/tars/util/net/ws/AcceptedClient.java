package com.tars.util.net.ws;

import org.slf4j.*;

import java.io.*;
import java.util.*;

import javax.websocket.*;
import javax.websocket.MessageHandler.*;

import rx.Observable;
import rx.subjects.*;
import util.*;


class AcceptedClient extends Endpoint implements WebsocketClient {

  Logger log = LoggerFactory.getLogger(WebsocketClient.class);

  Server server;
  MutableOption<Session> session = MutableOption.empty();
  PublishSubject<String> messageStream = PublishSubject.create();
  LinkedList<String> pendingMessages = new LinkedList<>();

  AcceptedClient(Server server) {
    this.server = server;
  }

  // websocket client interface

  @Override
  public WebsocketClient start() {
    log.warn("accepted clients are already started, this methods does nothing");
    return this;
  }

  @Override
  public WebsocketClient stop() {
    log.debug("stopping");
    if (session.isPresent()) {
      try {
        session.get().close();
      } catch (IOException e) {
        messageStream.onError(e);
      }
    }

    return this;
  }

  @SuppressWarnings("Duplicates")
  @Override
  public WebsocketClient send(String message) {
    session
        .ifPresent(session -> {
          log.debug("sending : " + message);
          session.getAsyncRemote().sendText(message);
        })
        .ifNotPresent(() -> {
          log.debug("no session available, adding to pending messages");
          pendingMessages.add(message);
        });

    return this;
  }

  @Override
  public Observable<String> stream() {
    return messageStream;
  }

  @Override
  public boolean isAlive() {
    return session.isPresent() && !messageStream.hasCompleted();
  }

  // endpoint interface

  @Override
  public void onOpen(final Session session, EndpointConfig ec) {
    log.debug("initializing session");
    this.session.take(session);

    while (!pendingMessages.isEmpty()) {
      String message = pendingMessages.pollFirst();
      log.debug("sending a pending message : {}", message);
      session.getAsyncRemote().sendText(message);
    }

    session.addMessageHandler((Whole<String>) new Whole<String>() {
      @Override
      public void onMessage(String message) {
        log.debug("streaming incoming message : {}", message);
        messageStream.onNext(message);
      }
    });

    log.debug("registering in servers clients list");
    server.clients.add(this);
    server.clientStream.onNext(this);
  }

  @Override
  public void onClose(Session session, CloseReason closeReason) {
    log.debug("closing session");
    this.session.clear();
    messageStream.onCompleted();

    log.debug("removing from servers clients list");
    server.clients.remove(this);
    server = null;
  }

  @Override
  public void onError(Session session, Throwable error) {
    messageStream.onError(error);
  }
}