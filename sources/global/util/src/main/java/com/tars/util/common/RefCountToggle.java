package com.tars.util.common;

import static com.tars.util.validation.Validator.condition;
import static com.tars.util.validation.Validator.notNull;

@Deprecated
public class RefCountToggle {

  private final Runnable start;
  private final Runnable stop;

  private int refCount = 0;

  public RefCountToggle(Runnable onStart, Runnable onStop) {
    condition(notNull(onStart) || notNull(onStop));
    this.start = onStart;
    this.stop = onStop;
  }

  public void increment() {
    if (refCount == 0) {
      start.run();
    }
    refCount++;
  }

  public void decrement() {
    if (refCount == 0) {
      return; // cant go negative
    } else if (refCount == 1) {
      stop.run();
    }
    refCount--;
  }

  public void reset() {
    if (refCount != 0) {
      stop.run();
    }

    refCount = 0;
  }

  public int count() {
    return refCount;
  }
}
