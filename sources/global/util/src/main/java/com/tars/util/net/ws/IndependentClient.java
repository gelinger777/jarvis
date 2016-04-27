package com.tars.util.net.ws;

import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.websocket.*;
import javax.websocket.MessageHandler.*;

import rx.Observable;
import rx.subjects.*;
import util.*;

class IndependentClient extends Endpoint implements WebsocketClient {

  Logger log = LoggerFactory.getLogger(WebsocketClient.class);

  URI address;
  MutableOption<Session> session = MutableOption.empty();
  PublishSubject<String> subject = PublishSubject.create();
  LinkedList<String> pending = new LinkedList<>();

  IndependentClient(String address) {
    this.address = URI.create(address);
  }

  // websocket client interface

  @Override
  public WebsocketClient start() {
    if (isAlive()) {
      log.info("already started");
      return this;
    }

    log.debug("starting");

    try {
      session.take(
          ContainerProvider
              .getWebSocketContainer()
              .connectToServer(this, address)
      );
    } catch (Exception ignore) {
      // exception reaching here will be already passed to onError
    }

    return this;
  }

  @Override
  public WebsocketClient stop() {
    if (session.isPresent()) {
      try {
        session.get().close();
      } catch (IOException e) {
        subject.onError(e);
      }
    }

    return this;
  }

  @Override
  public WebsocketClient send(String message) {
    session
        .ifPresent(session -> {
            log.debug("sending : " + message);
            session.getAsyncRemote().sendText(message);
        })
        .ifNotPresent(() -> {
          log.debug("no session available, adding to pending messages");
          pending.add(message);
        });

    return this;
  }

  @Override
  public Observable<String> stream() {
    return subject;
  }

  @Override
  public boolean isAlive() {
    return session.isPresent() && !subject.hasCompleted();
  }

  // endpoint interface

  @Override
  public void onOpen(final Session session, EndpointConfig ec) {
    log.debug("initializing session");
    session.setMaxIdleTimeout(0);
    session.setMaxTextMessageBufferSize(100000);
    this.session.take(session);

    while (!pending.isEmpty()) {
      String message = pending.pollFirst();
      log.debug("sending a pending message : {}", message);
      session.getAsyncRemote().sendText(message);
    }

    session.addMessageHandler((Whole<String>) new Whole<String>() {
      @Override
      public void onMessage(String message) {
        log.debug("streaming incoming message : {}", message);
        subject.onNext(message);
      }
    });
  }

  @Override
  public void onClose(Session session, CloseReason closeReason) {
    log.debug("closing session : " + closeReason.getReasonPhrase());
    this.session.clear();
    subject.onCompleted();
  }

  @Override
  public void onError(Session session, Throwable error) {
    if (error.getMessage().contains("Connection refused")) {
      log.warn("could not connect");
    } else {
      subject.onError(error);
    }
  }

}
