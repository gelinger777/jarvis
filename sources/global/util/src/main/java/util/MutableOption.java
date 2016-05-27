package util;

import java.util.*;
import java.util.function.*;

import static java.util.Objects.*;

/**
 * Mutable implementation of Optional with different interface and additional methods, and yeah its mutable...
 */
@SuppressWarnings("unchecked")
public final class MutableOption<T> {

  private volatile Object value;

  // constructors

  private MutableOption() {
  }

  private MutableOption(T value) {
    this.value = requireNonNull(value);
  }

  // static factory methods

  public static <T> MutableOption<T> empty() {
    return new MutableOption<>();
  }

  public static <T> MutableOption<T> of(T value) {
    return new MutableOption<>(value);
  }

  public static <T> MutableOption<T> ofNullable(T value) {
    return value == null ? empty() : of(value);
  }

  public Option<T> immutable() {
    return Option.ofNullable((T) value);
  }

  public T get() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }

    return (T) value;
  }

  public boolean isPresent() {
    return value != null;
  }

  public boolean isNotPresent() {
    return value == null;
  }

  public MutableOption<T> ifPresent(Consumer<? super T> consumer) {
    if (value != null) {
      try {
        consumer.accept((T) value);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  public MutableOption<T> filter(Predicate<? super T> predicate) {
    requireNonNull(predicate);
    if (isPresent() && !predicate.test((T) value)) {
      value = null;
    }
    return this;
  }

  public <U> MutableOption<U> map(Function<? super T, ? extends U> mapper) {
    requireNonNull(mapper);

    if (isPresent()) {
      value = mapper.apply((T) value);
    }

    return (MutableOption<U>) this;
  }

  public <U> MutableOption<U> flatMap(Function<? super T, MutableOption<U>> mapper) {
    requireNonNull(mapper);

    if (isPresent()) {
      return mapper.apply((T) value);
    }

    return (MutableOption<U>) this;
  }

  /**
   * If value is not present use provided one.
   */
  public <U> MutableOption<U> ifNotPresentTake(U other) {
    if (value == null) {
      value = requireNonNull(other);
    }

    return (MutableOption<U>) this;
  }

  /**
   * If value is not present use provided one.
   */
  public <U> MutableOption<U> ifNotPresentCompute(Supplier<U> supplier) {
    if (value == null) {
      value = requireNonNull(supplier.get());
    }

    return (MutableOption<U>) this;
  }

  /**
   * Accept value regardless of current state.
   */
  public <U> MutableOption<U> take(U other) {
    value = requireNonNull(other);
    return (MutableOption<U>) this;
  }

  /**
   * Accept value regardless of current state.
   */
  public <U> MutableOption<U> takeNullable(U other) {
    value = other;
    return (MutableOption<U>) this;
  }

  /**
   * Clear the value (same as empty optional).
   */
  public MutableOption<T> clear() {
    value = null;
    return this;
  }

  /**
   * Throw exception if value is not present.
   */
  public <X extends Throwable> MutableOption<T> ifNotPresentThrow(Supplier<? extends X> exceptionSupplier)
      throws X {
    if (value == null) {
      throw exceptionSupplier.get();
    }
    return this;
  }

  /**
   * If not present execute action.
   */
  public MutableOption<T> ifNotPresent(Runnable action) {
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

    if (!(obj instanceof MutableOption)) {
      return false;
    }

    MutableOption<?> other = (MutableOption<?>) obj;
    return Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return value != null
           ? String.format("MutableOption[%s]", value)
           : "MutableOption.empty";
  }
}
