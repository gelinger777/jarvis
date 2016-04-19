package com.tars.util.net.socket;


import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

import rx.Observable;
import rx.subjects.*;
import util.*;

import static java.lang.Thread.*;
import static java.nio.channels.SelectionKey.*;

/**
 * SocketHub provides interface to create socket client and server instances and interact with those using observable
 * streams.
 *
 * All Client and Server instances share single event loop for NIO processing.
 */
public class SocketHub {

  private static final Logger log = LoggerFactory.getLogger(SocketHub.class);

  private volatile Selector selector;
  private volatile boolean hasScheduledTasks;
  private final LinkedList<Runnable> tasks = new LinkedList<>();

  private final Map<SelectionKey, SocketClientImpl> activeClients = new HashMap<>();
  private final Map<SelectionKey, SocketServerImpl> activeServers = new HashMap<>();

  // event loop logic
  private final Runnable eventLoopRunnable = () -> {
    try (Selector selector = Selector.open()) {
      // initialize global hub selector
      this.selector = selector;
      log.info("event loop is initialized");

      // actual event loop
      while (true) {
        log.debug("waiting for next event or task");

        // block until an event arrives or selector is woken up or event loop is interrupted
        selector.select();

        // execute events

        Iterator keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
          SelectionKey key = (SelectionKey) keys.next();
          keys.remove();

          if (!key.isValid()) {
            continue;
          }

          if (key.isReadable()) {
            log.debug("reading");
            try {
              this.read(key);
            } catch (Throwable cause) {
              this.handleClientError(key, cause);
            }
          } else if (key.isWritable()) {
            try {

              log.debug("writing");
              this.write(key);
            } catch (Throwable cause) {
              this.handleClientError(key, cause);
            }
          } else if (key.isConnectable()) {
            try {
              log.debug("connecting");
              this.connect(key);
            } catch (Throwable cause) {
              this.handleClientError(key, cause);
            }
          } else if (key.isAcceptable()) {
            try {
              log.debug("accepting");
              this.accept(key);
            } catch (Throwable cause) {
              this.handleServerError(key, cause);
            }
          } else {
            throw new IllegalStateException("unknown key state");
          }
        }

        // execute tasks

        while (hasScheduledTasks) {
          Runnable task;

          // get the next task
          synchronized (tasks) {
            task = tasks.pollFirst();
            if (tasks.isEmpty()) {
              hasScheduledTasks = false;
            }
          }

          log.debug("executing a task");
          task.run();
        }

        if (currentThread().isInterrupted() && !hasScheduledTasks) {
          log.info("event loop successfully completed");
          break;
        }
      }
    } catch (Exception cause) {
      this.handleHubError(cause);
    }
  };

  private final RefCountTask eventLoopTask = cpu.refCountTask("socket-hub-event-loop", eventLoopRunnable);

  // interface

  public synchronized SocketServer server(String host, int port) {
    SocketServerImpl server = new SocketServerImpl();

    server.address = new InetSocketAddress(host, port);
    server.isAlive = false;
    server.channel = null;

    log.info("server created : {}", server);
    return server;
  }

  public synchronized SocketClient client(String host, int port) {
    SocketClientImpl client = new SocketClientImpl();

    client.key = null;
    client.channel = null;
    client.framer = null;
    client.address = new InetSocketAddress(host, port);
    client.isAlive = false;
    log.info("created client : {}", client);

    return client;
  }

  public synchronized SocketClient client(InetSocketAddress address) {
    SocketClientImpl client = new SocketClientImpl();

    client.key = null;
    client.channel = null;
    client.framer = null;
    client.address = address;
    client.isAlive = false;
    log.debug("created client : {}", client);

    return client;
  }

  public boolean isPortAvailable(int port) {
    try (ServerSocket ignored = new ServerSocket(port)) {
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  // lifecycle

  public SocketHub() {
    log.info("init");
  }

  public void release() {
    log.info("shutdown");
    activeClients.values().forEach(SocketClientImpl::stop);
    activeServers.values().forEach(SocketServer::stop);
    eventLoopTask.reset();
  }

  // event loop logic

  private void read(SelectionKey key) throws IOException {
    SocketClientImpl client = activeClients.get(key);

    log.debug("reading from channel to buffer");
    int read = client.framer.read();

    log.debug("publishing messages to the client stream");
    List<byte[]> unpublishedMessages = client.framer.flushRead();
    unpublishedMessages.forEach(client.subject::onNext);

    if (read == -1) {
      client.stop();
      key.cancel();
      activeClients.remove(key);
    }
  }

  private void write(SelectionKey key) throws IOException {
    log.debug("writing to client channel");

    activeClients.get(key).framer.flushWrite();
    key.interestOps(SelectionKey.OP_READ);
  }

  private void connect(SelectionKey key) throws IOException {
    log.debug("finishing connection client");

    // finish connect
    SocketClientImpl client = activeClients.get(key);
    client.channel.finishConnect();

    // check if we have pending messages
    if (!client.pending.isEmpty()) {
      do {
        client.framer.write(client.pending.poll());
      } while (!client.pending.isEmpty());
      key.interestOps(SelectionKey.OP_WRITE);
    } else {
      key.interestOps(SelectionKey.OP_READ);
    }
  }

  private void accept(SelectionKey key) throws IOException {
    SocketServerImpl server = activeServers.get(key);

    // get client channel
    SocketChannel clientChannel = server.channel.accept();
    clientChannel.configureBlocking(false);

    // register interest in reading
    SelectionKey clientKey = clientChannel.register(selector, OP_READ);

    SocketClientImpl client = new SocketClientImpl();

    client.key = clientKey;
    client.address = clientChannel.getRemoteAddress();
    client.channel = clientChannel;
    client.framer = new ByteFramer(clientChannel, 1024);
    client.isAlive = true;
    eventLoopTask.increment();
    log.info("{} : created (accepted) client {}", server, client);

    activeClients.put(clientKey, client);
    server.subject.onNext(client);
  }

  // error handling

  /**
   * Attempts to handle client error, if another exception is thrown it is passed to hub error handling.
   */
  private void handleClientError(SelectionKey key, Throwable cause) throws IOException {
    log.warn("handling client error");
    SocketClientImpl client = activeClients.remove(key);

    if (client.isAlive) {
      client.log.debug("{} : stopping", client);

      if (client.channel != null) {
        client.channel.close();
      }

      client.subject.onError(cause);
      client.isAlive = false;
    }

    key.cancel();
  }

  /**
   * Attempts to handle server error, if exception is thrown it is passed to hub error handling.
   */
  private void handleServerError(SelectionKey key, Throwable cause) throws IOException {
    log.warn("handling server error");
    SocketServerImpl server = activeServers.remove(key);

    if (server.isAlive) {
      server.log.debug("{} : stopping", server);

      server.clients.forEach(SocketClient::stop);
      server.channel.close();
      server.subject.onError(cause);
      server.isAlive = false;
    }

    key.cancel();
  }

  /**
   * Shuts down the hub, as the error is not recoverable.
   */
  private void handleHubError(Throwable cause) {
    log.error("hub error", cause);

    log.debug("shutting down, scheduling stop for all clients and servers of the hub");
    activeClients.values().forEach(SocketClientImpl::stop);
    activeServers.values().forEach(SocketServer::stop);

    log.debug("completing the event loop");
    eventLoopTask.reset();
  }

  // inner implementation of SocketServer interface

  private class SocketClientImpl implements SocketClient {

    final Logger log = LoggerFactory.getLogger(SocketClientImpl.class);

    SocketAddress address;
    PublishSubject<byte[]> subject = PublishSubject.create();
    SocketChannel channel;
    ByteFramer framer;
    SelectionKey key;
    volatile boolean isAlive;

    LinkedList<byte[]> pending = new LinkedList<>();

    // interface

    @Override
    public SocketClient start() {
      if (isAlive) {
        log.debug("{} : already started", this);
        return this;
      }

      log.debug("{} : scheduling start", this);
      eventLoopTask.increment();
      SocketHub.this.schedule(() -> {
        try {
          log.debug("{} : starting", this);

          channel = SocketChannel.open();
          channel.configureBlocking(false);
          channel.connect(address);
          key = channel.register(SocketHub.this.selector, SelectionKey.OP_CONNECT);
          framer = new ByteFramer(channel, 1024);
          isAlive = true;

          SocketHub.this.activeClients.put(key, this);
        } catch (Exception cause) {
          log.error("{} : unexpected exception while starting", this);
          subject.onError(cause);
        }
      });

      return this;
    }

    @Override
    public SocketClient stop() {
      if (!isAlive) {
        log.debug("{} : already stopped", this);
        return this;
      }

      log.debug("{} : scheduling stop", this);
      SocketHub.this.schedule(() -> {
        try {
          log.debug("{} : stopping", this);

          channel.close();
          subject.onCompleted();
          activeClients.remove(key);
        } catch (Exception cause) {
          log.error("{} : unexpected exception while stopping", this);
          subject.onError(cause);
        }
        eventLoopTask.decrement();
        isAlive = false;
      });

      return this;
    }

    @Override
    public SocketClient send(byte[] message) {
      log.debug("{} : scheduling send [{}]", this, Arrays.toString(message));
      SocketHub.this.schedule(() -> {
        if (isAlive) {
          framer.write(message);
          key.interestOps(SelectionKey.OP_WRITE);
        } else {
          pending.add(message);
          log.debug("{} : client is not started, send is pending", this);
        }
      });

      return this;
    }

    @Override
    public Observable<byte[]> stream() {
      log.debug("{} : getting the data stream", this);
      return subject;
    }

    @Override
    public boolean isAlive() {
      return isAlive;
    }
  }

  // inner implementation of SocketClient interface

  private class SocketServerImpl implements SocketServer {

    final Logger log = LoggerFactory.getLogger(SocketServerImpl.class);

    SocketAddress address;
    PublishSubject<SocketClient> subject = PublishSubject.create();
    LinkedList<SocketClient> clients = new LinkedList<>();
    ServerSocketChannel channel;
    SelectionKey key;
    volatile boolean isAlive;

    // interface

    @Override
    public SocketServer start() {
      if (isAlive) {
        log.debug("{} : already started", this);
        return this;
      }

      log.debug("{} : scheduling start", this);
      eventLoopTask.increment();
      SocketHub.this.schedule(() -> {
        try {
          log.debug("{} : starting", this);

          channel = ServerSocketChannel.open();
          channel.configureBlocking(false);
          channel.socket().bind(address);
          key = channel.register(SocketHub.this.selector, SelectionKey.OP_ACCEPT);
          isAlive = true;

          SocketHub.this.activeServers.put(key, this);

        } catch (Exception cause) {
          log.error("{} : unexpected exception while starting", this);
          subject.onError(cause);
        }
      });

      return this;
    }

    @Override
    public SocketServer stop() {
      if (!isAlive) {
        log.debug("{} : already stopped", this);
        return this;
      }

      log.debug("{} : scheduling stop", this);
      SocketHub.this.schedule(() -> {
        try {
          log.debug("{} : stopping", this);

          while (!clients.isEmpty()) {
            clients.poll().stop();
          }

          channel.close();
          key.cancel();
          subject.onCompleted();
        } catch (Exception cause) {
          log.error("{} : unexpected exception while stopping", this);
          subject.onError(cause);
        }

        eventLoopTask.decrement();
        isAlive = false;
      });

      return this;
    }

    @Override
    public Observable<SocketClient> stream() {
      log.debug("{} : getting the stream", this);
      return subject;
    }

    @Override
    public boolean isAlive() {
      return isAlive;
    }
  }

  // stuff

  /**
   * Schedule a task for event loop execution.
   *
   * If current thread is the event loop it will run immediately.
   */
  private boolean schedule(Runnable task) {
    if (eventLoopTask.isAlive()) {

      if (eventLoopTask.isCurrentThread()) {
        log.trace("already on event loop executing");
        task.run();
      } else {
        synchronized (tasks) { // disrupt
          tasks.add(task);
        }
        hasScheduledTasks = true;
        log.trace("task is scheduled to event loop");
        if (selector != null) {
          selector.wakeup();
        }
      }
      return true;
    } else {
      log.warn("event loop is not alive, cannot schedule");
      return false;
    }
  }
}
