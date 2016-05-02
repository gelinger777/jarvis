package com.tars.util.net.streamer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.subjects.PublishSubject;
import util.cpu.executors;

import static util.cpu.sleep;
import static util.global.ExceptionsKt.executeAndGetSilent;
import static util.global.ValidationKt.condition;
import static util.global.ValidationKt.notNull;

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
    condition(notNull(callable), "condition failure");
    this.callable = callable;
    this.subject = PublishSubject.create();
    this.streaming = false;
  }

  public SingleThreadStreamer(Callable<T> callable, int delay) {
    this(callable);
    condition(delay > 0, "condition failure");
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

    executors.INSTANCE.getIo().submit(() -> {
      while (isStreaming()) {
        log.trace("executing");
        executeAndGetSilent(callable)
            .ifPresent(subject::onNext)
            .ifNotPresent(() -> log.warn("broke with exception"));

        if (delay > 0) {
          log.trace("sleeping");
          sleep(delay);
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
