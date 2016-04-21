package com.tars.util.common;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public class RefCountHolder<T> {

  private final RefCountToggle toggle;
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

  @NotNull
  public T requestInstance() {
    toggle.increment();
    return instance;
  }


  public void returnInstance(T instance) {
    if (instance == null) {
      throw new NullPointerException();
    }

    if (instance != this.instance) {
      throw new IllegalStateException("resource does not belong to holder");
    }

    toggle.decrement();
  }
}
