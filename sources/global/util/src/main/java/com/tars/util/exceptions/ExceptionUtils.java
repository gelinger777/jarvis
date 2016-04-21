package com.tars.util.exceptions;

import org.slf4j.*;

import java.util.concurrent.*;
import java.util.function.*;

import javaslang.control.Try.*;
import util.*;

import static com.tars.util.validation.Validator.*;

/**
 * Utility class for exception handling and boilerplate elimination.
 *
 * note : logger with file appender must be configured for ExceptionUtils class.
 */
@Deprecated
public class ExceptionUtils {

  /**
   * Appropriate file appender shall be defined in logging configuration file.
   */
  public static final Logger log = LoggerFactory.getLogger("exceptions");

  // wtf methods

  /**
   * All overloaded wtf() methods are logging the provided data (if any) to a file and killing the process, these
   * represent use cases that shall never happen in production.
   *
   * P.S. WTF stands for (what a terrible failure)
   */
  public static <T> T wtf() {
    return reportAndDie(new WTFException());
  }

  /**
   * All overloaded wtf() methods are logging the provided data (if any) to a file and killing the process, these
   * represent use cases that shall never happen in production.
   *
   * P.S. WTF stands for (what a terrible failure)
   */
  public static <T> T wtf(String message) {
    return reportAndDie(new WTFException(message));
  }

  /**
   * All overloaded wtf() methods are logging the provided data (if any) to a file and killing the process, these
   * represent use cases that shall never happen in production.
   *
   * P.S. WTF stands for (what a terrible failure)
   */
  public static <T> T wtf(String message, Object... args) {
    return reportAndDie(
        new WTFException(
            String.format(message, args)
        )
    );
  }

  /**
   * All overloaded wtf() methods are logging the provided data (if any) to a file and killing the process, these
   * represent use cases that shall never happen in production.
   *
   * P.S. WTF stands for (what a terrible failure)
   */
  public static <T> T wtf(Throwable cause) {
    return reportAndDie(
        new WTFException(cause)
    );
  }

  /**
   * All overloaded wtf() methods are logging the provided data (if any) to a file and killing the process, these
   * represent use cases that shall never happen in production.
   *
   * P.S. WTF stands for (what a terrible failure)
   */
  public static <T> T wtf(Throwable cause, String message) {
    return reportAndDie(
        new WTFException(message, cause)
    );
  }

  /**
   * All overloaded wtf() methods are logging the provided data (if any) to a file and killing the process, these
   * represent use cases that shall never happen in production.
   *
   * P.S. WTF stands for (what a terrible failure)
   */
  public static <T> T wtf(Throwable cause, String message, Object... args) {
    return reportAndDie(new WTFException(
        String.format(message, args), cause
    ));
  }

  /**
   * Placeholder for work in progress.
   */
  public static <T> T notImplemented() {
    return reportAndDie(new WTFException("not implemented"));
  }

  // hooks

  private static final Option<Consumer<Throwable>> logCallback = Option.<Consumer<Throwable>>empty();

  /**
   * Sets callback to be executed when unrecoverable exception is about to kill the process. This can be used to notify
   * about critical failure or for graceful shutdown.
   */
  public static void onUnrecoverableFailure(Consumer<Throwable> task) {
    logCallback
        .map(consumer -> consumer.andThen(task))
        .ifNotPresentTake(task);
  }

  // logging to file

  /**
   * Logs an exception to general error log and returns without throwing it further.
   */
  public static void report(Throwable cause) {
    condition(notNull(cause));
    log.warn("unexpected exception", cause);
  }

  /**
   * Logs an exception to general error log and returns without throwing it further.
   */
  public static void report(Throwable cause, String message) {
    condition(notNull(cause) && notNullOrEmpty(message));
    log.warn(message, cause);
  }

  /**
   * If cause is checked exception wraps to RuntimeException
   */
  public static RuntimeException wrap(Throwable cause) {
    condition(notNull(cause));
    if (cause instanceof RuntimeException) {
      return (RuntimeException) cause;
    } else {
      return new RuntimeException(cause);
    }
  }

  /**
   * Logs an exception to general error log, if cause is checked exception wraps to RuntimeException and rethrows.
   */
  public static <T> T reportAndThrow(Throwable cause) {
    report(cause);
    throw wrap(cause);
  }

  /**
   * Logs an exception to general error log, if cause is checked exception wraps and returns a RuntimeException.
   */
  public static RuntimeException reportAndWrap(Throwable cause) {
    report(cause);
    return wrap(cause);
  }

  /**
   * Logs an exception to general error log with specified message, if cause is checked exception wraps and returns a
   * RuntimeException.
   */
  public static RuntimeException reportAndWrap(Throwable cause, String message) {
    report(cause, message);
    return wrap(cause);
  }

  private static <T> T reportAndDie(Throwable cause) {
    log.error("unrecoverable exception", cause);
    logCallback.ifPresent(throwableConsumer -> {
      throwableConsumer.accept(cause);
    });
    System.exit(-1); // kill process
    return null;
  }

  // execution utilities

  /**
   * Executes runnable, if any exception is thrown logs it and ignores.
   */
  public static void executeSilent(CheckedRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable cause) {
      report(cause);
    }
  }

  /**
   * Executes runnable, if any exception is thrown logs it, wraps to RuntimeException and rethrows.
   */
  public static void execute(CheckedRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable cause) {
      reportAndThrow(cause);
    }
  }

  /**
   * Executes runnable, if any exception is thrown logs it, executes callbacks if any, AND KILLS THE PROCESS.
   */
  public static void executeMandatory(CheckedRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable cause) {
      wtf(cause);
    }
  }

  // callable

  /**
   * Executes callable, wraps in Option, if any exception is thrown logs it and returns empty option.
   */
  public static <T> Option<T> executeAndGetSilent(Callable<T> callable) {
    try {
      return Option.ofNullable(callable.call());
    } catch (Throwable cause) {
      report(cause);
      return Option.<T>empty();
    }
  }

  /**
   * Executes callable, if callable returns null or any exception is thrown logs it, wraps to RuntimeException and
   * rethrows.
   */
  public static <T> T executeAndGet(Callable<T> callable) {
    try {
      T result = callable.call();

      if (result != null) {
        return result;
      } else {
        return reportAndThrow(new RuntimeException("callable returned null"));
      }
    } catch (Throwable cause) {
      return reportAndThrow(cause);
    }
  }

  /**
   * Executes callable, if callable returns null or any exception is thrown logs it, executes callbacks if any, AND
   * KILLS THE PROCESS.
   */
  public static <T> T executeAndGetMandatory(Callable<T> callable) {
    try {
      T result = callable.call();

      if (result != null) {
        return result;
      } else {
        throw new RuntimeException("callable returned null");
      }
    } catch (Throwable cause) {
      return wtf(cause);
    }
  }
}

