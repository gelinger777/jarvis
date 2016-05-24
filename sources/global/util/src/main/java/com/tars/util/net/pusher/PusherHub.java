package com.tars.util.net.pusher;

import com.pusher.client.*;
import com.pusher.client.channel.*;

import org.slf4j.*;

import rx.*;
import rx.subjects.*;

public class PusherHub {

  private final Logger log = LoggerFactory.getLogger("pusher");

  public Observable<String> stream(String pusherKey, String channelKey, String eventType) {
    Pusher pusher = new Pusher(pusherKey);
    pusher.connect();


    Channel channel = pusher.subscribe(channelKey);

    PublishSubject<String> subject = PublishSubject.create();

    channel.bind(eventType, (ch, ev, data) -> {
      log.debug("{} : {} : {}", ch, ev, data);
      subject.onNext(data);
    });

    return subject;
  }
}
