package com.tars.util.storage;

import com.tars.util.concurrent.ConcurrencyUtils;
import com.tars.util.concurrent.RefCountTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.yield;

class Watchers {

  static final Logger log = LoggerFactory.getLogger(Watchers.class);

  static final CopyOnWriteArrayList<Watcher> activeWatchers = new CopyOnWriteArrayList<>();

  static final RefCountTask eventLoop = ConcurrencyUtils.refCountTask("chronicle-watcher-event-loop", () -> {
    log.debug("event loop started");

    // while not interrupted
    while (!currentThread().isInterrupted()) {
      activeWatchers.forEach(Watcher::checkAndEmit);
      yield();
    }

    log.debug("event loop completed");
  });

  static final Map<String, Watcher> allWatchers = new HashMap<>();

  static Watcher get(String path) {
    return allWatchers.computeIfAbsent(path, Watcher::new);
  }

  static void add(Watcher watcher) {
    activeWatchers.add(watcher);
    eventLoop.increment();
  }

  static void remove(Watcher watcher) {
    activeWatchers.remove(watcher);
    eventLoop.decrement();
  }
}
