package com.tars.controller.btce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import proto.Messages.Order;
import proto.Messages.Pair;
import proto.Messages.Trade;
import rx.Observable;
import com.tars.common.GenericApiClient;
import com.tars.common.ProtoBufUtil;
import com.tars.util.Option;

import static com.tars.util.exceptions.ExceptionUtils.notImplemented;
import static com.tars.util.validation.Validator.condition;
import static com.tars.util.validation.Validator.notNull;

public class BtceClient implements GenericApiClient {

  final static Logger log = LoggerFactory.getLogger(BtceClient.class);

  // configuration

  BtceSignature signature;
  Map<Pair, TradePoller> streamers = new HashMap<>();


  public BtceClient(BtceSignature signature) {
//    condition(notNull(signature));
    this.signature = signature;

//    NetworkUtils.http().getString(get("https://btc-e.com/api/3/info"))
//        .ifPresent(info -> supportedPairs.addAll(
//            new JsonParser().parse(info).getAsJsonObject().get("pairs").getAsJsonObject()
//                .entrySet().stream().map(pair -> Parser.parsePair(pair.getKey()))
//                .collect(Collectors.toList())));
  }

  // lifecycle

//  @Override
//  public void start() {
//  }
//
//  @Override
//  public void stop() {
//  }

  // trading stream

  @Override
  public Observable<Trade> streamTrades(Pair pair) {
    condition(notNull(pair));

    log.debug("starting trade stream : {}", ProtoBufUtil.json(pair));

    return streamers.computeIfAbsent(
        pair,
        key -> new TradePoller("https://btc-e.com/api/3/trades/" + Parser.translate(pair))
            .start()
    ).stream();
  }

  @Override
  public Set<Pair> activeTradeStreams() {
    return streamers.keySet();
  }

  @Override
  public void closeTradeStream(Pair pair) {
    condition(streamers.containsKey(pair));
    log.debug("closing trade stream : {}", ProtoBufUtil.json(pair));

    streamers
        .remove(pair)
        .stop();
  }

  // orderbook stream

  @Override
  public Observable<Order> streamOrderbook(Pair pair) {
    return notImplemented();
  }

  @Override
  public Set<Pair> activeOrderbookStreams() {
    return notImplemented();
  }

  @Override
  public void closeOrderbookStream(Pair pair) {
    notImplemented();
  }

  // account stream

//  @Override
//  public Observable<AccountActivity> streamAccountActivity() {
//    return notImplemented();
//  }
//
//  @Override
//  public void closeActivityStream() {
//    notImplemented();
//  }

  // actions

  @Override
  public Option<Order> bid(Pair pair, double amount, double price) {
    return notImplemented();
  }

  @Override
  public Option<Order> ask(Pair pair, double amount, double price) {
    return notImplemented();
  }

  @Override
  public Option<Boolean> cancel(Pair pair, String id) {
    return notImplemented();
  }
}
