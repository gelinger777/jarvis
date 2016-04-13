package com.tars.util.validation;

import java.util.Collection;

import static java.util.Arrays.asList;

public abstract class Validator {

  // exception throwers

  /**
   * If expression is false throws IllegalStateException
   */
  public static void condition(boolean condition) throws IllegalStateException {
    if (!condition) {
      throw new IllegalStateException();
    }
  }

  /**
   * If expression is false throws IllegalStateException with specified message
   *
   * @throws IllegalStateException with provided message
   */
  public static void condition(boolean condition, String message) throws IllegalStateException {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  /**
   * If expression is false throws IllegalStateException with specified message
   *
   * @throws IllegalStateException with provided message formatted
   */
  public static void condition(boolean condition, String message, Object... args)
      throws IllegalStateException {
    condition(condition, String.format(message, args));
  }

  // condition checkers

  // null checking
  public static boolean isNull(Object object) {
    return object == null;
  }

  public static boolean notNull(Object object) {
    return object != null;
  }

  // vararg null checking (all, any)

  public static boolean anyNull(Object... objects) {
    condition(allNotNullOrEmpty(objects), "at least one argument is expected");
    for (Object argument : objects) {
      if (argument == null) {
        return true;
      }
    }
    return false;
  }

  public static boolean allNull(Object... objects) {
    condition(allNotNullOrEmpty(objects), "at least one argument is expected");
    for (Object argument : objects) {
      if (argument == null) {
        return true;
      }
    }
    return false;
  }

  public static boolean allNotNull(Object... objects) {
    condition(allNotNullOrEmpty(objects), "at least one argument is expected");
    for (Object argument : objects) {
      if (argument == null) {
        return false;
      }
    }
    return true;
  }

  // equality

  /**
   * @return true if all objects are equal, false if any of objects is not equal or is null
   */
  public static boolean areEqual(Object... objects) {
    condition(allNotNullOrEmpty(objects), "at least one argument is expected");

    if (anyNull(objects)) {
      return false;
    }

    Object previous = objects[0];
    for (int i = 1; i < objects.length; i++) {
      if (!objects[i].equals(previous)) {
        return false;
      } else {
        previous = objects[i];
      }
    }
    return true;
  }

  public static boolean notEqual(Object... objects) {
    if (anyNull(objects)) {
      return false;
    }
    if (objects.length == 1) {
      return true;
    }

    Object previous = objects[0];
    for (int i = 1; i < objects.length; i++) {
      if (!objects[i].equals(previous)) {
        return true;
      } else {
        previous = objects[i];
      }
    }
    return false;
  }

  // not null or empty string/collection

  public static boolean notNullOrEmpty(String string) {
    return string != null && string.length() > 0;
  }

  public static boolean allNotNullOrEmpty(Object... objects) {
    return objects != null && objects.length > 0;
  }

  public static boolean notNullOrEmpty(Collection collection) {
    return collection != null && !collection.isEmpty();
  }

  public static boolean isNullOrEmpty(String string) {
    return string == null || string.length() == 0;
  }

  public static boolean isNullOrEmpty(Object... objects) {
    return objects == null || objects.length == 0;
  }

  public static boolean isNullOrEmpty(Collection collection) {
    return collection == null || collection.isEmpty();
  }

  // all not null or empty

  public static boolean allNotNullOrEmpty(String... strings) {
    condition(notNullOrEmpty(strings), "at least one argument is expected");

    for (String string : strings) {
      if (isNullOrEmpty(string)) {
        return false;
      }
    }
    return true;
  }

  public static boolean notNullOrEmpty(Object[] objects) {
    return objects != null && objects.length > 0;
  }

  public static boolean allNotNullOrEmpty(Collection... collections) {
    condition(notNullOrEmpty(collections), "at least one argument is expected");

    for (Collection collection : collections) {
      if (isNullOrEmpty(collection)) {
        return false;
      }
    }
    return true;
  }

  public static boolean allNotNullOrEmpty(Collection<String> strings) {
    if (strings == null || strings.size() == 0) {
      return false;
    }
    for (String str : strings) {
      if (isNullOrEmpty(str)) {
        return false;
      }
    }
    return true;
  }

  // uppercase/lowercase

  public static boolean isUppercase(String string) {
    return string != null && string.equals(string.toUpperCase());
  }

  public static boolean areUpperCase(String... strings) {
    for (String string : strings) {
      if (!isUppercase(string)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isLowercase(String string) {
    return string != null && string.equals(string.toLowerCase());
  }

  // compare ignore order

  public static boolean compareIgnoreOrder(Object[] expected, Object[] actual) {
    return asList(expected).containsAll(asList(actual));
  }

  public static boolean notNullOrEmpty(byte[] data) {
    return data != null && data.length > 0;
  }
}
