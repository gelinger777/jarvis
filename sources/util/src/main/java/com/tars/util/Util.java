//package com.tars.util;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Collection;
//
//
//import static util.global.ExceptionsKt.*;
//import static util.global.FunctionsKt.*;
//import static util.global.ValidationKt.*;
//
//public class Util {
//
//  public static final Logger logger = LoggerFactory.getLogger(Util.class);
//
//  public static double roundDown5(double d) {
//    return Math.floor(d * 1e5) / 1e5;
//  }
//
//  // unchecked cast
//
//  /**
//   * Attempts to cast to desired runtime type, if any exception takes place it is logged to exception log and thrown
//   * further up the chain.
//   */
//  @SuppressWarnings("unchecked")
//  public static <T> T cast(Object object) {
//    try {
//      return (T) object;
//    } catch (ClassCastException cause) {
//      throw report(cause, "");
//    }
//  }
//
//  // hex
//
//  private final static char[] HEXES = "0123456789ABCDEF".toCharArray();
//
//  public static String bytesToHex(byte[] bytes) {
//    final StringBuilder result = new StringBuilder(2 * bytes.length);
//    for (byte b : bytes) {
//      result
//          .append(HEXES[(b & 0xF0) >> 4])
//          .append(HEXES[(b & 0x0F)]);
//    }
//    return result.toString();
//  }
//
//  /**
//   * Current time in seconds (unix time)
//   */
//  public static long unixTime() {
//    return System.currentTimeMillis() / 1000L;
//  }
//
//  public static String absolutePathOf(String relative) {
//    return Paths.get(relative).toAbsolutePath().toString();
//  }
//
//  /**
//   * Order matters, all arguments must properly implement hashcode, nulls are omitted.
//   */
//  private static int hashcodeForTuple(Object... objects) {
//    final int prime = 31;
//    int result = 1;
//    for (Object object : objects) {
//      if (object != null) {
//        result = result * prime + object.hashCode();
//      }
//    }
//    return result;
//  }
//
//  public static <T> Collection<T> join(Collection<T>... others) {
//    condition(notNullOrEmpty(others));
//
//    ArrayList<T> result = new ArrayList<>();
//
//    for (Collection<T> collection : others) {
//      result.addAll(collection);
//    }
//
//    return result;
//  }
//}
