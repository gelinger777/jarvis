package com.tars.controller.btce;

import com.tars.common.*;
import com.tars.util.net.*;
import com.tars.util.net.streamer.*;

import java.util.*;

import proto.common.*;
import rx.Observable;
import rx.subjects.*;
import util.*;

import static com.tars.controller.btce.BtceClient.*;
import static org.apache.http.client.methods.RequestBuilder.*;

class TradePoller {

  SingleThreadStreamer<List<Trade>> streamer;
  PublishSubject<Trade> stream;
  long lastTimeStamp;

  public TradePoller(String url) {
    lastTimeStamp = System.currentTimeMillis();

    stream = PublishSubject.create();

    streamer = new SingleThreadStreamer<>(
        () -> {
          try {
            log.debug("polling : " + url);
            Option<String> response = NetworkUtils.http().getString(get(url));

            log.debug("converting : " + url);
            List<Trade> trades = response
                .map(Parser::parseTrades)
//              .<List<Trade>>ifNotPresentTake(Collections::emptyList)
                .get();

            return trades;

          } catch (Exception e) {
            log.error("WTF?", e);
          }
          return new ArrayList<>();
        },
        2000
    );

    streamer
        .stream()
        .subscribe(this::filterAndEmit);
  }

  public TradePoller start() {
    streamer.start();
    return this;
  }

  public TradePoller stop() {
    streamer.stop();
    stream.onCompleted();
    return this;
  }

  private void filterAndEmit(List<Trade> trades) {
    for (Trade trade : trades) {
      if (trade.getTime() <= lastTimeStamp) {
        continue;
      }

      lastTimeStamp = trade.getTime();
      log.debug("trade emitted : {}", ProtoBufUtil.json(trade));
      stream.onNext(trade);
    }
  }

  public Observable<Trade> stream() {
    return stream;
  }

}
