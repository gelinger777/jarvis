package com.tars.exchanges.bitfinex;

import proto.Messages.Pair;
import rx.subjects.PublishSubject;

class Channel<T> {

  final Type type;
  final PublishSubject<T> stream = PublishSubject.create();

  Integer chanId;
  Pair pair;

  public Channel(Type type) {
    this.type = type;
  }

  public PublishSubject<T> stream() {
    return stream;
  }

  public Channel<T> chanId(Integer chanId) {
    this.chanId = chanId;
    return this;
  }

  public Channel<T> pair(Pair pair) {
    this.pair = pair;
    return this;
  }

  public enum Type {
    TRADE,
    ORDERBOOK
  }

}
