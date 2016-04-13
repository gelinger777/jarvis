package com.tars.util.concurrent;


import com.tars.util.common.RefCountToggle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tars.util.concurrent.ConcurrencyUtils.daemon;
import static com.tars.util.exceptions.ExceptionUtils.executeMandatory;

public class RefCountTask {


  private static final Logger log = LoggerFactory.getLogger(RefCountTask.class);

  private Thread thread;
  RefCountToggle toggle;

  Runnable start, stop;

  public RefCountTask(String name, Runnable runnable) {
    this(name, runnable, 10000);
  }

  public RefCountTask(String name, Runnable runnable, long terminationTimeout) {

    // define behavior of start
    start = () -> {
      // start the task
      log.trace("starting the task");
      thread = daemon(name, runnable);
      thread.start();
    };

    // define behaviour of stop
    stop = () -> {
      // stop the task wait for completion for 10 seconds
      log.trace("stopping the task");
      thread.interrupt();

      // if decrement was not called from runnable itself
      if (thread != Thread.currentThread()) {
        // block till the thread shuts down
        executeMandatory(() -> thread.join(terminationTimeout));
      }
      thread = null;
    };

    // instantiate toggle
    this.toggle = new RefCountToggle(start, stop);

    log.debug("reference counting task created : {}", name);
  }


  public synchronized void increment() {
    System.err.println("INCREMENT");
    toggle.increment();
  }

  public synchronized void decrement() {
    System.err.println("DECREMENT");
    toggle.decrement();
  }

  public synchronized void reset() {
    toggle.reset();
  }

  public boolean isCurrentThread() {
    return thread != null && Thread.currentThread().equals(thread);
  }

  public boolean isAlive() {
    return thread != null && thread.isAlive();
  }
}
