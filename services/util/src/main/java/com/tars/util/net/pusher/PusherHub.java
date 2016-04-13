package com.tars.util.net.pusher;


import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.tars.util.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.subjects.PublishSubject;


public class PusherHub {

  private static final Logger log = LoggerFactory.getLogger(PusherHub.class);

  private static final Map<String, PusherWrapper> pushers = new HashMap<>();

  // interface

  public static synchronized Observable<String> stream(String pusherKey, String channelKey, String eventKey) {
    return pushers
        // create or get existing pusher wrapper
        .computeIfAbsent(pusherKey, (key) -> {
          log.info("creating pusher : {}", key);
          return new PusherWrapper(new Pusher(pusherKey));
        })
        // start a stream
        .stream(channelKey, eventKey);
  }

  public static synchronized void close(String pusherKey, String channelKey, String eventKey) {
    Option.ofNullable(pushers.get(pusherKey))
        .ifPresent(pusher -> {
          pusher.close(channelKey, eventKey);
          if (pusher.channels.isEmpty()) {
            pusher.pusher.disconnect();
            log.info("closing pusher : {}", pusherKey);
            pushers.remove(pusherKey);
          }
        })
        .ifNotPresent(() -> log.debug("no pusher instance to cancel"));
  }

  // types

  private static class PusherWrapper {

    Pusher pusher;
    Map<String, ChannelWrapper> channels = new HashMap<>();

    PusherWrapper(Pusher pusher) {
      this.pusher = pusher;
      pusher.connect();
    }

    Observable<String> stream(String channelKey, String eventKey) {
      return channels
          // create or get existing channel wrapper
          .computeIfAbsent(channelKey, key -> {
            log.info("subscribing to channel : {}", key);
            return new ChannelWrapper(pusher.subscribe(key));
          })
          // stream for event
          .stream(eventKey);
    }

    void close(String channelKey, String eventKey) {
      Option.ofNullable(channels.get(channelKey))
          .ifPresent(channel -> {
            channel.close(eventKey);
            if (channel.streams.isEmpty()) {
              pusher.unsubscribe(channelKey);
              log.info("closing channel {}", channelKey);
              channels.remove(channelKey);
            }
          })
          .ifNotPresent(() -> log.debug("no channel to unsubscribe"));
    }
  }

  private static class ChannelWrapper {

    Channel channel;
    Map<String, StreamWrapper> streams = new HashMap<>();

    ChannelWrapper(Channel channel) {
      this.channel = channel;
    }

    Observable<String> stream(String eventKey) {
      return streams
          .computeIfAbsent(eventKey, key -> {
            log.info("binding to event stream : {}", eventKey);
            return new StreamWrapper(channel, eventKey);
          })
          .subject;
    }

    void close(String eventKey) {
      Option.ofNullable(streams.get(eventKey))
          .ifPresent((stream) -> {
            stream.close();
            log.info("closing stream : {}", eventKey);
            streams.remove(eventKey);
          })
          .ifNotPresent(() -> log.debug("no channel to unsubscribe"));
    }


  }

  private static class StreamWrapper implements SubscriptionEventListener {

    Channel channel;
    String eventKey;
    PublishSubject<String> subject = PublishSubject.create();

    StreamWrapper(Channel channel, String eventKey) {
      this.channel = channel;
      this.eventKey = eventKey;
      channel.bind(eventKey, this);
    }

    @Override
    public void onEvent(String channel, String event, String data) {
      log.debug("{} : {} : {}", channel, event, data);
      subject.onNext(data);
    }

    void close() {
      log.debug("unbind listener of {} from channel", eventKey);
      channel.unbind(eventKey, this);
      subject.onCompleted();
    }
  }
}
