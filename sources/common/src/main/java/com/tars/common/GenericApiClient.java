package com.tars.common;

import util.Option;

import java.util.Set;

import proto.common.Order;
import proto.common.Pair;
import proto.common.Trade;
import rx.Observable;

import static com.tars.util.exceptions.ExceptionUtils.notImplemented;

/**
 * Generic api client that shall provide all the necessary functionality for an exchange.
 */
@Deprecated
public interface GenericApiClient {

  // lifecycle

  default void start() {
    notImplemented();
  }

  default void stop() {
    notImplemented();
  }

  // trading stream

  Observable<Trade> streamTrades(Pair pair);

  Set<Pair> activeTradeStreams();

  void closeTradeStream(Pair pair);

  // orderbook stream

  Observable<Order> streamOrderbook(Pair pair);

  Set<Pair> activeOrderbookStreams();

  void closeOrderbookStream(Pair pair);

  // account stream

//  Observable<AccountActivity> streamAccountActivity();
//
//  void closeActivityStream();

  // actions

  Option<Order> bid(Pair pair, double amount, double price);

  Option<Order> ask(Pair pair, double amount, double price);

  Option<Boolean> cancel(Pair pair, String id);
}
