package com.tars.util.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to provide event driven parser for text, using regular expressions to define events and capture their
 * arguments. Unlike sax parsers this is not xml specific, and there is no need to handle all standard events (like tag
 * opened, tag closed etc) to maintain complex state (the more possible events the complex it is to maintain the state),
 * instead client code can define events and arguments those shall provide.
 *
 * This class is not thread safe (for parallel processing thread confinement via ThreadLocals is preferred).
 */
public class StreamingTextParser {

  private final Set<String> eventTypes;
  private final Map<String, Handler> handlerMap;
  private final Handler onCompleted;

  private final Set<EventTracker> trackers;

  StreamingTextParser(Set<String> eventTypes, Map<String, Handler> handlerMap, Handler onCompleted) {
    this.eventTypes = eventTypes;
    this.handlerMap = handlerMap;
    this.trackers = new HashSet<>();
    this.onCompleted = onCompleted;
  }

  public void parse(String text) {
    try {
      // init matcher map
      for (String type : eventTypes) {
        trackers.add(new EventTracker(type, text));
      }

      // stream events
      while (true) {
        int position = text.length();
        EventTracker closest = null;

        // find closest event
        for (EventTracker tracker : trackers) {
          int trackerPosition = tracker.position;
          if (!tracker.isCompleted && trackerPosition < position) {
            closest = tracker;
            position = trackerPosition;
          }
        }

        // execute appropriate handler for the event
        if (closest != null) {
          handlerMap.get(closest.type).handle(closest.matcher);
          closest.findNext();
        }
        // no more events left
        else {
          if (onCompleted != null) {
            onCompleted.handle(null);
          }
          break;
        }
      }

    } catch (Exception cause) {
      throw new IllegalStateException(cause);
    } finally {
      trackers.clear();
    }
  }

  public interface Handler {

    void handle(Matcher matcher);
  }

  // builder

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final Set<String> eventTypes = new HashSet<>();
    private final Map<String, Handler> handlerMap = new HashMap<>();
    private Handler handler;

    private Builder() {
    }

    public Builder registerHandler(String regexp, Handler handler) {

      if (regexp == null || regexp.isEmpty() || handler == null) {
        throw new IllegalArgumentException();
      }

      eventTypes.add(regexp);
      handlerMap.put(regexp, handler);

      return this;
    }

    public Builder onCompleted(Handler handler) {
      if (handler == null) {
        throw new IllegalArgumentException();
      }
      this.handler = handler;
      return this;
    }

    public StreamingTextParser build() {
      return new StreamingTextParser(eventTypes, handlerMap, handler);
    }
  }

  private static class EventTracker {

    String type;
    Matcher matcher;

    int position = -1;
    boolean isCompleted = false;

    EventTracker(String regex, String text) {
      this.type = regex;
      this.matcher = Pattern.compile(type).matcher(text);
      findNext();
    }

    public void findNext() {
      if (isCompleted) {
        return;
      }

      if (matcher.find()) {
        position = matcher.start();
        isCompleted = false;
      } else {
        position = -1;
        isCompleted = true;
      }
    }
  }
}