package com.tars.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import rx.Scheduler;

import static com.tars.util.exceptions.ExceptionUtils.executeMandatory;
import static com.tars.util.exceptions.ExceptionUtils.executeSilent;
import static com.tars.util.exceptions.ExceptionUtils.onUnrecoverableFailure;
import static com.tars.util.validation.Validator.condition;
import static com.tars.util.validation.Validator.notNull;
import static com.tars.util.validation.Validator.notNullOrEmpty;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static rx.schedulers.Schedulers.from;


public class ConcurrencyUtils {

  private static final Logger log = LoggerFactory.getLogger(ConcurrencyUtils.class);

  public static class Executors {

    private static ForkJoinPool cp;
    private static ExecutorService io;
    private static ScheduledExecutorService sch;

    /**
     * ExecutorService intended for computation and pure cpu work.
     */
    public static ExecutorService cp() {
      return cp;
    }

    /**
     * ExecutorService intended for non computational tasks (like IO).
     */
    public static ExecutorService io() {
      return io;
    }

    /**
     * ScheduledExecutorService for application wide usage.
     */
    public static ScheduledExecutorService sch() {
      return sch;
    }
  }

  public static class Schedulers {

    private static Scheduler cp;
    private static Scheduler io;

    /**
     * Scheduler intended for computation and pure cpu work.
     */
    public static Scheduler cp() {
      return cp;
    }

    /**
     * Scheduler intended for non computational tasks (like IO).
     */
    public static Scheduler io() {
      return io;
    }

  }
  // lifecycle

  public static void init() {

    Executors.cp = commonPool();
    Executors.io = newCachedThreadPool();
    Executors.sch = newScheduledThreadPool(20, daemonThreadFactory());
    log.debug("executors are initialized");

    Schedulers.cp = from(Executors.cp);
    Schedulers.io = from(Executors.io);
    log.debug("schedulers are initialized");

    onUnrecoverableFailure(throwable -> close());
    log.info("initialized");
  }

  public static void close() {
    // common pool doesn't need shutdown
    Executors.io.shutdown();

    log.debug("waiting for pools to shut down");

    executeMandatory(() -> Executors.io.awaitTermination(1, TimeUnit.MINUTES));
    executeMandatory(() -> Executors.cp.awaitQuiescence(1, TimeUnit.MINUTES));

    log.info("closed");
  }

  // sleep

  public static void bearSleep(long millis) {
    executeSilent(() -> TimeUnit.MILLISECONDS.sleep(millis));
  }

  public static void bearSleep(long value, TimeUnit unit) {
    executeSilent(() -> unit.sleep(value));
  }

  // daemon thread factory

  private static final ThreadFactory threadFactory = runnable -> {
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    return thread;
  };

  public static ThreadFactory daemonThreadFactory() {
    return threadFactory;
  }

  public static Thread thread(String name, Runnable runnable) {
    condition(notNullOrEmpty(name));
    return createThread(false, name, runnable);
  }

  public static Thread daemon(String name, Runnable runnable) {
    condition(notNullOrEmpty(name));
    return createThread(true, name, runnable);
  }


  /**
   * Runnable must support interruption.
   */
  @Deprecated
  public static RefCountTask refCountTask(String name, Runnable runnable) {
    condition(notNullOrEmpty(name) && notNull(runnable));
    return new RefCountTask(name, runnable);
  }

  /**
   * Runnable must support interruption.
   */

  @Deprecated
  public static RefCountTask refCountTask(String name, Runnable runnable, long terminationTimeout) {
    condition(notNullOrEmpty(name) && notNull(runnable) && terminationTimeout > 0);
    return new RefCountTask(name, runnable, terminationTimeout);
  }

  private static Thread createThread(boolean daemon, String name, Runnable runnable) {

    // consider builder for creating threads
    condition(notNull(runnable));

    Thread thread = new Thread(runnable);

    if (daemon) {
      thread.setDaemon(true);
    }

    if (notNullOrEmpty(name)) {
      thread.setName(name);
    }

    return thread;
  }
}
