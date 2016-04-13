package com.tars.exchanges.bitfinex;

import java.util.List;

import proto.Messages.Order;

public interface OrderbookAggregator {

  void apply(Order order);

  List<Order> bids();

  List<Order> asks();
}
