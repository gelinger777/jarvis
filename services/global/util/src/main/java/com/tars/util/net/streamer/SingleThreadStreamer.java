package com.tars.util.net.streamer;


import com.tars.util.concurrent.ConcurrencyUtils.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.tars.util.concurrent.ConcurrencyUtils.bearSleep;
import static com.tars.util.exceptions.ExceptionUtils.executeAndGetSilent;
import static com.tars.util.validation.Validator.condition;
import static com.tars.util.validation.Validator.notNull;

/**
 * Streamer will keep executing provided callable and emit values trough observable until its stopped. It is using
 * single thread and allows to specify delay between calls. If call fails it will not terminate the streamer.
 */
public class SingleThreadStreamer<T> {

  private static final Logger log = LoggerFactory.getLogger(SingleThreadStreamer.class);

  private final Callable<T> callable;
  private final PublishSubject<T> subject;

  private volatile boolean streaming;
  private int delay = 0;

  public SingleThreadStreamer(Callable<T> callable) {
    condition(notNull(callable));
    this.callable = callable;
    this.subject = PublishSubject.create();
    this.streaming = false;
  }

  public SingleThreadStreamer(Callable<T> callable, int delay) {
    this(callable);
    condition(delay > 0);
    this.delay = delay;
  }

  // behaviour

  /**
   * Observable of values queried
   */
  public Observable<T> stream() {
    return subject;
  }

  public SingleThreadStreamer<T> start() {

    if (streaming) {
      log.trace("already streaming");
      return this;
    }

    streaming = true;

    Executors.io().submit(() -> {
      while (isStreaming()) {
        log.trace("executing");
        executeAndGetSilent(callable)
            .ifPresent(subject::onNext)
            .ifNotPresent(() -> log.warn("broke with exception"));

        if (delay > 0) {
          log.trace("sleeping");
          bearSleep(delay);
        }
      }
    });

    return this;
  }

  public synchronized SingleThreadStreamer<T> stop() {
    streaming = false;
    return this;
  }

  public synchronized boolean isStreaming() {
    return streaming;
  }
}
