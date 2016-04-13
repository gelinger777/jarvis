package com.tars.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Alternative implementation of java.util.Optional with different interface and additional methods, and yeah its
 * mutable...
 */
@SuppressWarnings("unchecked")
public final class Option<T> {

  private Object value;

  // constructors

  private Option() {
  }

  private Option(T value) {
    this.value = requireNonNull(value);
  }

  // static factory methods

  public static <T> Option<T> empty() {
    return new Option<>();
  }

  public static <T> Option<T> of(T value) {
    return new Option<>(value);
  }

  public static <T> Option<T> ofNullable(T value) {
    return value == null ? empty() : of(value);
  }

  // interface

  public T get() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }

    return (T) value;
  }

  public boolean isPresent() {
    return value != null;
  }

  public Option<T> ifPresent(Consumer<? super T> consumer) {
    if (value != null) {
      try {
        consumer.accept((T) value);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  public Option<T> filter(Predicate<? super T> predicate) {
    requireNonNull(predicate);
    if (isPresent() && !predicate.test((T) value)) {
      value = null;
    }
    return this;
  }

  public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
    requireNonNull(mapper);

    if (isPresent()) {
      value = mapper.apply((T) value);
    }

    return (Option<U>) this;
  }

  public <U> Option<U> flatMap(Function<? super T, Option<U>> mapper) {
    requireNonNull(mapper);

    if (isPresent()) {
      return mapper.apply((T) value);
    }

    return (Option<U>) this;
  }

  /**
   * If value is not present use provided one.
   */
  public <U> Option<U> ifNotPresentTake(U other) {
    if (value == null) {
      value = requireNonNull(other);
    }

    return (Option<U>) this;
  }

  /**
   * If value is not present use provided one.
   */
  public <U> Option<U> ifNotPresentTake(Supplier<U> supplier) {
    if (value == null) {
      value = requireNonNull(supplier.get());
    }

    return (Option<U>) this;
  }

  /**
   * Accept value regardless of current state.
   */
  public <U> Option<U> take(U other) {
    value = requireNonNull(other);
    return (Option<U>) this;
  }

  /**
   * Accept value regardless of current state.
   */
  public <U> Option<U> takeNullable(U other) {
    value = other;
    return (Option<U>) this;
  }

  /**
   * Clear the value (same as empty optional).
   */
  public Option<T> clear() {
    value = null;
    return this;
  }

  /**
   * Throw exception if value is not present.
   */
  public <X extends Throwable> Option<T> ifNotPresentThrow(Supplier<? extends X> exceptionSupplier)
      throws X {
    if (value == null) {
      throw exceptionSupplier.get();
    }
    return this;
  }

  /**
   * If not present execute action.
   */
  public Option<T> ifNotPresent(Runnable action) {
    if (value == null) {
      action.run();
    }
    return this;
  }

  // equals, hashcode and toString (these are delegated to actual value)

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Option)) {
      return false;
    }

    Option<?> other = (Option<?>) obj;
    return Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return value != null
           ? String.format("Option[%s]", value)
           : "Option.empty";
  }
}
