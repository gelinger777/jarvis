package com.tars.util.storage;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.tars.util.exceptions.ExceptionUtils.executeAndGet;
import static com.tars.util.validation.Validator.condition;
import static com.tars.util.validation.Validator.notNull;
import static com.tars.util.validation.Validator.notNullOrEmpty;

/**
 * Utility for managing IndexedChronicle instances.
 */
class Chronicles {

  static final Logger log = LoggerFactory.getLogger(Chronicles.class);
  static Map<String, Chronicle> chronicles = new HashMap<>();
  static Map<String, ExcerptAppender> appenders = new HashMap<>();

  static void register(String path, Chronicle chronicle) {
    condition(notNullOrEmpty(path) && !chronicles.containsKey(path) && notNull(chronicle));

    chronicles.put(path, chronicle);
  }

  /**
   * Instantiates tailer for chronicle.
   */
  static ExcerptTailer tailer(String path) {
    condition(notNullOrEmpty(path));
    return executeAndGet(() -> chronicles.get(path).createTailer());
  }

  /**
   * Instantiate (or reuse) appender for chronicle.
   */
  static ExcerptAppender appender(String path) {
    condition(notNullOrEmpty(path));

    return executeAndGet(
        () -> appenders.computeIfAbsent(
            path,
            key -> executeAndGet(() -> chronicles.get(key).createAppender())
        )
    );
  }


  static long size(String path) {
    return chronicles.get(path).lastIndex();
  }

  // stuff

  private static Chronicle createOrGetChronicle(String path) {
    Chronicle chronicle = chronicles.get(path);

    if (chronicle == null) {
      log.trace("creating chronicle '{}'", path);
      chronicle = executeAndGet(() -> ChronicleQueueBuilder.indexed(path).small().build());
      chronicles.put(path, chronicle);
    } else {
      log.trace("reusing previously created wrapper");
    }

    return chronicle;
  }

  // utility methods

  static byte[] readFrame(ExcerptTailer tailer) {
    byte[] result = new byte[tailer.readInt()];
    tailer.read(result);
    tailer.finish();
    return result;
  }

  static void writeFrame(ExcerptAppender appender, byte[] data) {
    int msgSize = 4 + data.length;
    appender.startExcerpt(msgSize);
    appender.writeInt(data.length);
    appender.write(data);
    appender.finish();
  }
}
