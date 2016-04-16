package com.tars.util.common;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RefCountHolder<T> {

  private RefCountToggle toggle;
  private T instance = null;

  public RefCountHolder(Supplier<T> supplier, Consumer<T> finalizer) {
    toggle = new RefCountToggle(
        () -> instance = supplier.get(),
        () -> {
          finalizer.accept(instance);
          instance = null;
        }
    );
  }

  public T requestInstance() {
    toggle.increment();
    return instance;
  }

  public void returnInstance(T instance) {
    if (instance == this.instance) {
      toggle.decrement();
    } else {
      throw new IllegalStateException("resource does not belong to holder");
    }
  }
}
