package com.tars.common;

import proto.Messages.Currency;
import proto.Messages.Order;
import proto.Messages.Pair;
import proto.Messages.Trade;

import static java.lang.String.format;

/**
 * As we cannot customize protobuf toString, we will use this utility instead.
 */
public final class ProtoBufUtil {

  public static String json(Currency currency) {
    return currency.getSymbol();
  }

  public static String json(Pair pair) {
    return format("{%s | %s}", pair.getBase().getSymbol(), pair.getQuote().getSymbol());
  }

  public static String json(Order order) {
    return format("{%f | %f}", order.getPrice(), order.getVolume());
  }

  public static String json(Trade trade) {
    return "{ " + trade.getTime() + " | " + trade.getPrice() + " | " + trade.getVolume() + " }";
  }

  public static String tradeStreamName(String exchangeName, Pair pair) {
    return format(
        "TRADE|%s|%s|%s",
        exchangeName,
        pair.getBase().getSymbol(),
        pair.getQuote().getSymbol()
    );
  }
}
