package com.tars.util.health;


import com.tars.util.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.tars.util.health.HearBeatWatchdog.registry;
import static com.tars.util.validation.Validator.condition;
import static com.tars.util.validation.Validator.notNull;
import static com.tars.util.validation.Validator.notNullOrEmpty;
import static java.util.Collections.unmodifiableMap;

public class Pulse {

  private static final Logger log = LoggerFactory.getLogger(Pulse.class);

  final String name;
  final long timeout;
  final Runnable callback;

  long lastBeat;
  boolean isAlive = false;

  /**
   * Create pulse.
   */
  public static Pulse of(String name, long timeout, Runnable callback) {
    condition(notNullOrEmpty(name) && notNull(callback) && timeout > 0);
    // check if there is already pulse started
    if (registry.containsKey(name)) {
      log.debug("reusing existing pulse instance : {}", name);
      return registry.get(name);
    }
    // create pulse (yet to be started)
    else {
      log.debug("creating pulse : {}", name);
      return new Pulse(name, timeout, callback);
    }
  }

  /**
   * Get existing pulse.
   */
  public static Option<Pulse> of(String name) {
    condition(notNullOrEmpty(name));
    return Option.ofNullable(registry.get(name));
  }

  /**
   * Get all pulses.
   */
  public static Map<String, Pulse> all() {
    return unmodifiableMap(HearBeatWatchdog.registry);
  }

  Pulse(String name, long timeout, Runnable callback) {
    this.name = name;
    this.timeout = timeout;
    this.callback = callback;
  }

  /**
   * Start monitoring the pulse. If timeout is reached callback will be executed.
   */
  public synchronized Pulse start() {
    // check if is already started
    if (!isAlive) {
      lastBeat = System.currentTimeMillis();
      isAlive = true;
      HearBeatWatchdog.add(this);
    }
    return this;
  }

  /**
   * Register a heartbeat.
   */
  public synchronized Pulse beat() {
    if (isAlive) {
      lastBeat = System.currentTimeMillis();
      log.debug("{} : heartbeat", name);
    } else {
      log.warn("{} : cant beat (not started)");
    }

    return this;
  }

  /**
   * Stop monitoring the pulse.
   */

  public synchronized Pulse stop() {
    if (isAlive) {
      isAlive = false;
      HearBeatWatchdog.remove(this);
    } else {
      log.warn("{} : cant stop (not started)");
    }
    return this;
  }

  synchronized boolean isFine() {
    long timeSinceLastHB = System.currentTimeMillis() - lastBeat;

    log.trace("time since last hb : {}, timeout : {}", timeSinceLastHB, timeout);
    // pulse is fine if it was disabled or acceptable timeout has not been violated
    return isAlive && System.currentTimeMillis() - lastBeat < timeout;
  }
}
