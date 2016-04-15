package com.tars.controller.btce;

import java.util.ArrayList;
import java.util.List;

import proto.Trade;
import rx.Observable;
import rx.subjects.PublishSubject;
import com.tars.common.ProtoBufUtil;
import util.Option;
import com.tars.util.net.NetworkUtils;
import com.tars.util.net.streamer.SingleThreadStreamer;

import static com.tars.controller.btce.BtceClient.log;
import static org.apache.http.client.methods.RequestBuilder.get;

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
