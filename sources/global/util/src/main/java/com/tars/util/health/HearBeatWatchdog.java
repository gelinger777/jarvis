package com.tars.util.health;

import com.tars.util.concurrent.ConcurrencyUtils;
import com.tars.util.concurrent.RefCountTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.tars.util.concurrent.ConcurrencyUtils.refCountTask;
import static java.lang.Thread.currentThread;

@Deprecated
class HearBeatWatchdog {

  static final Logger log = LoggerFactory.getLogger(HearBeatWatchdog.class);

  static final Map<String, Pulse> registry = new ConcurrentHashMap<>();

  static final RefCountTask watchDogTask = refCountTask("heartbeat-watchdog", () -> {
    log.debug("heartbeat watchdog starting");

    while (!currentThread().isInterrupted()) {

      // iterate over all monitored instances
      for (Pulse pulse : registry.values()) {
        log.trace("checking : {}", pulse.name);
        // filter those who violated the timeout
        if (!pulse.isFine()) {
          // schedule the callback execution
          log.warn("heartbeat violation : {}", pulse.name);
          ConcurrencyUtils.Executors.io().submit(pulse.callback);
          remove(pulse);
        }
      }

      // support interruption
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException ignored) {
        // heartbeat watchdog is not needed anymore (ref count = 0)
        break;
      }
    }
    log.debug("heartbeat watchdog completed");
  });

  static void add(Pulse pulse) {
    registry.putIfAbsent(pulse.name, pulse);
    watchDogTask.increment();
    log.debug("registered pulse : {}", pulse.name);
  }

  static void remove(Pulse pulse) {
    registry.remove(pulse.name);
    watchDogTask.decrement();
    log.debug("unregistered pulse : {}", pulse.name);
  }
}
