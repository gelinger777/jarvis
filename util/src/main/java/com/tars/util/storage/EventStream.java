package com.tars.util.storage;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import util.net;

import static com.tars.util.exceptions.ExceptionUtils.executeAndGet;
import static com.tars.util.validation.Validator.condition;


/**
 * By default all observables are operating on current thread or on event loop of watcher (if its a realtime stream),
 * make sure to provide appropriate schedulers to subscribeOn and observeOn.
 */
public abstract class EventStream {

  private static final Logger log = LoggerFactory.getLogger(EventStream.class);
  private static Map<String, EventStream> streams = new HashMap<>();

  /**
   * Creates or gets existing chronicle
   */
  public static synchronized EventStream get(String path) {
    return streams.computeIfAbsent(path, (key) -> {

      // create chronicle
      Chronicle chronicle = executeAndGet(() -> ChronicleQueueBuilder
          .indexed(path)
          .small()
          .build()
      );

      log.info("created chronicle at {}", path);

      // register chronicle for further use
      Chronicles.register(path, chronicle);

      // create an event stream
      return new EventStreamImpl(path);
    });
  }

  /**
   * Creates or gets existing chronicle source
   */
  public static EventStream get(String path, int port) {

    return streams.computeIfAbsent(path, (key) -> {
      // make sure port is free
      condition(net.INSTANCE.getSocket().isPortAvailable(port));

      // create chronicle
      Chronicle chronicle = executeAndGet(() -> ChronicleQueueBuilder
          .indexed(path)
          .small()
          .source()
          .bindAddress(port).build()
      );

      log.info("created chronicle source  at {} : {}", port, path);

      // register chronicle for further use
      Chronicles.register(path, chronicle);

      // create an event stream
      return new EventStreamImpl(path);
    });
  }

  public abstract Observable<byte[]> stream();

  public abstract Observable<byte[]> streamFromStart();

  public abstract Observable<byte[]> streamFrom(long from);

  public abstract Observable<byte[]> streamFromTo(long from, long to);

  public abstract void append(byte[] data);

  public abstract long size();
}
