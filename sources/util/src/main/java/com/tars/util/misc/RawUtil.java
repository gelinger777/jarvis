//package com.tars.util.misc;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static util.global.ValidationKt.allNotNullOrEmpty;
//import static util.global.ValidationKt.condition;
//import static util.global.ValidationKt.notNull;
//
///**
// * Provides utility methods to work with raw lists and maps.
// */
//@Deprecated
//@SuppressWarnings("unchecked")
//public class RawUtil {
//
//  /**
//   * Retrieves a value.
//   *
//   * @param data root container (either list or map)
//   * @param path array of keys composing a complete path trough root container to target data
//   * @param <T>  type of the expected value
//   */
//  public static <T> T retrieve(Object data, Object... path) throws IllegalStateException {
//    validateContainer(data);
//    validatePath(path);
//
//    try {
//      Object key = path[path.length - 1]; // data key
//
//      if (key instanceof String) {
//        return (T) ((Map) container(data, false, path)).get(key);
//      }
//      if (key instanceof Integer) {
//        return (T) ((List) container(data, false, path)).get((int) key);
//      }
//
//    } catch (Exception e) {
//      throw new IllegalStateException(e);
//    }
//
//    throw new IllegalStateException();
//  }
//
//  // stuff
//
//  /**
//   * Retrieve the container of target data
//   *
//   * @param container      root container
//   * @param createIfAbsent permission to dynamically add containers according to path (when adding data)
//   * @param path           array of keys composing a complete path trough root container to target data
//   * @throws Exception call to this method shall always be wrapped in try catch block
//   */
//  private static Object container(Object container, boolean createIfAbsent, Object... path) throws Exception {
//    int lastIndex = path.length - 1;
//
//    // navigate to actual container in tree
//    for (int i = 0; i < lastIndex; i++) {
//      Object key = path[i];
//
//      // type of current container
//      boolean isObject;
//      Object nextContainer;
//
//      // if key is string then current container is an object
//      if (key instanceof String) {
//        // set next container
//        nextContainer = ((Map) container).get(key);
//        isObject = true;
//
//        // if key is int then current container is a List
//      } else if (key instanceof Integer) {
//        // set next container
//        nextContainer = ((List) container).get((int) key);
//        isObject = false;
//      } else {
//        throw new IllegalStateException();
//      }
//
//      // if container exists go for next key
//      if (notNull(nextContainer)) {
//        container = nextContainer;
//        continue;
//
//        // otherwise if allowed to create non existing container
//      } else if (createIfAbsent) {
//        Object nextKey = path[i + 1];
//
//        // if next key is string then add map
//        if (nextKey instanceof String) {
//          // set next container
//          nextContainer = new HashMap();
//
//          if (isObject) {
//            container = ((Map) container).put(key, nextContainer);
//          } else {
//            container = ((List) container).add(nextContainer);
//          }
//          continue;
//        }
//
//        // if next key is int then create list
//        if (nextKey instanceof Integer) {
//          // set next container
//          nextContainer = new ArrayList();
//
//          if (isObject) {
//            container = ((Map) container).put(key, nextContainer);
//          } else {
//            container = ((List) container).add(nextContainer);
//          }
//          continue;
//        }
//      }
//
//      throw new IllegalStateException();
//    }
//
//    return container;
//  }
//
//  private static void validateContainer(Object container) throws IllegalStateException {
//    condition(
//        container instanceof Map ||
//        container instanceof List,
//        "instance of Map or List is expected"
//    );
//  }
//
//  private static void validatePath(String... path) throws IllegalStateException {
//    condition(allNotNullOrEmpty(path), "path should not be null or empty");
//
//    for (Object key : path) {
//      condition(notNull(key), "key should not be null");
//      condition(
//          (key instanceof String && !((String) key).isEmpty()) ||
//          (key instanceof Integer && (int) key >= 0),
//          "key can be either not empty string or positive integer"
//      );
//    }
//  }
//}
