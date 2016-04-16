package com.tars.util.storage;


import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

import static com.tars.util.exceptions.ExceptionUtils.report;
import static com.tars.util.validation.Validator.condition;
import static com.tars.util.validation.Validator.notNullOrEmpty;

class EventStreamImpl extends EventStream {

  static final Logger log = LoggerFactory.getLogger(EventStreamImpl.class);

  final String path;

  EventStreamImpl(String path) {
    this.path = path;
  }

  @Override
  public Observable<byte[]> stream() {
    log.info("streaming {}", path);
    return Watchers.get(path).stream();
  }

  @Override
  public Observable<byte[]> streamFromStart() {
    log.info("streaming {} from start", path);
    return observe(-1, -1);
  }

  @Override
  public Observable<byte[]> streamFrom(long from) {
    log.info("streaming {} from {}", path, from);
    condition(from >= 0);
    return observe(from, -1);
  }

  @Override
  public Observable<byte[]> streamFromTo(long from, long to) {
    log.info("streaming {} from {} to {}", path, from, to);
    condition(from >= 0 && to > from);
    return observe(from, to);
  }


  @Override
  public void append(byte[] data) {
    log.debug("appending to {}", path);
    condition(notNullOrEmpty(data));
    ExcerptAppender appender = Chronicles.appender(path);
    Chronicles.writeFrame(appender, data);
  }

  @Override
  public long size() {
    return Chronicles.size(path);
  }

  @Override
  public void close() {
    // todo
  }

  Observable<byte[]> observe(long from, long to) {
    return Observable.create(subscriber -> {
      ExcerptTailer tailer = null;
      Observable<byte[]> realtime = null;
      try {
        // get tailer for watcher
        tailer = Chronicles.tailer(path);

        // prepare realtime stream
        if (to == -1) {
          realtime = Watchers.get(path).stream();
        }

        // if from is specified set starting index
        if (from > 0) {
          tailer.index(from - 1);
        }

        log.debug("streaming from existing data");

        // while subscriber is subscribed keep streaming
        while (!subscriber.isUnsubscribed()) {

          // read next entry
          if (tailer.nextIndex()) {
            //publish data
            subscriber.onNext(Chronicles.readFrame(tailer));
          } else {
            break;
          }

          // if there was upper limit and limit is reached finish
          if (to != -1 && tailer.index() > to) {
            break;
          }
        }

        // start streaming from realtime stream
        if (!subscriber.isUnsubscribed() && to == -1) {
          log.debug("streaming from realtime stream");
          realtime.subscribe(subscriber);
          return;
        }

        // acknowledge completion
        subscriber.onCompleted();
      } catch (Throwable cause) {
        // publish error
        report(cause);
        subscriber.onError(cause);
      } finally {
        // release allocated resource
        if (tailer != null) {
          tailer.close();
          tailer = null;
        }
      }
    });
  }

//  /**
//   * Close chronicle and complete any currently streaming observables
//   */
//  public void close() {
//    closed = true;
//  }
}
