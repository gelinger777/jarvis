package com.tars.util.storage;

import com.tars.util.common.RefCountToggle;

import net.openhft.chronicle.ExcerptTailer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.tars.util.storage.Watchers.activeWatchers;

class Watcher {
  static final Logger log = LoggerFactory.getLogger(Watcher.class);

  final String path;
  final PublishSubject<byte[]> subject;
  final Observable<byte[]> observable;
  volatile ExcerptTailer tailer;

  Watcher(String path) {
    this.path = path;
    this.subject = PublishSubject.create();

    RefCountToggle toggle = new RefCountToggle(
        () -> {
          log.debug("client subscribed");
          tailer = Chronicles.tailer(path);
          tailer.toEnd();
          Watchers.add(this);
          log.debug("watching");
        },
        () -> {
          log.debug("client unsubscribed");
          Watchers.remove(this);
          tailer.close();
          tailer = null;
          log.debug("not watching");
        }
    );

    this.observable = subject
        .doOnSubscribe(toggle::increment)
        .doOnUnsubscribe(toggle::decrement);
  }

  /**
   * Returns an Observable that when subscribed will stream any new data for the IndexedChronicle queue.
   */
  Observable<byte[]> stream() {
    return observable;
  }


  void checkAndEmit() {
    if (subject.hasCompleted()) {
      log.trace("attempt to check removed watchable");
      // this can happen when watcher removed itself from array list,
      // but busy loop still iterates over old copy...
      return;
    }

    try {
      // if new data is available
      if (tailer != null && tailer.nextIndex()) {

        // read data from watcher
        log.trace("extracting");
        byte[] bytes = Chronicles.readFrame(tailer);

        // emit data
        log.trace("publishing");
        subject.onNext(bytes);
      }
    } catch (Exception cause) {
      log.error("unexpected exception", cause);

      // release resources of this watchable
      tailer.close();
      tailer = null;

      // make this reclaimable
      activeWatchers.remove(this);

      // publish the error
      subject.onError(cause);
    }
  }

//  public void stop() {
//    // all existing clients will be unsubscribed and unsubscribe toggle will be triggered
//    subject.onCompleted();
//  }
}