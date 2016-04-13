package com.tars.exchanges.bitfinex;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedList;
import java.util.List;

import proto.Messages.Order;
import proto.Messages.Order.Builder;
import proto.Messages.Pair;
import proto.Messages.Side;
import proto.Messages.Trade;
import com.tars.common.Util;
import com.tars.util.exceptions.ExceptionUtils;
import com.tars.util.health.Pulse;

import static com.tars.common.ProtoBufUtil.tradeStreamName;
import static com.tars.util.validation.Validator.condition;
import static proto.Messages.Side.ASK;
import static proto.Messages.Side.BID;

/**
 * Converts bitfinex json messages to generic messages
 */
class Parser {

  // parsers

  public static Trade parseTrade(Channel<Trade> tradeChannel, JsonArray array) {
    // heartbeat
    if (array.size() == 2) {
      Pair pair = tradeChannel.pair;
      Pulse.of(tradeStreamName("BITFINEX", pair))
          .ifPresent(Pulse::beat)
          .ifNotPresentTake(() -> ExceptionUtils.wtf("pulse instance not found"));
      return null;
    }

    JsonElement secondElement = array.get(1);
    if (!secondElement.isJsonArray() && secondElement.getAsString().equals("te")) {
      // new trade
      return Trade.newBuilder()
          .setTime(array.get(3).getAsLong() * 1000) // unix timestamp to normal
          .setPrice(array.get(4).getAsDouble())
          .setVolume(Math.abs(array.get(5).getAsDouble()))
          .build();
    } else {
      // snapshot
      return null;
    }
  }

  public static List<Order> parseOrderbook(JsonArray array) {

    LinkedList<Order> result = new LinkedList<>();

    // either heartbeat or single order
    JsonElement element = array.get(1);

    // check if its the first batch
    if (element.isJsonArray()) {
      Builder builder = Order.newBuilder();

      // add all orders in batch
      element.getAsJsonArray().forEach(jsonOrder -> {

        JsonArray orderArray = jsonOrder.getAsJsonArray();
        double price = orderArray.get(1).getAsDouble();
        double volume = orderArray.get(2).getAsDouble();
        Side side = (volume > 0) ? BID : ASK;

        result.add(
            builder
                .setPrice(price)
                .setVolume(volume)
                .setSide(side)
                .build()
        );

        builder.clear();
      });
    } else if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isNumber()) {
//        long id = primitive.getAsLong();
        double price = array.getAsJsonArray().get(2).getAsDouble();
        double volume = array.getAsJsonArray().get(3).getAsDouble();
        Side side = (volume > 0) ? BID : ASK;

        result.add(
            Order.newBuilder()
//                .setId(id)
                .setPrice(price)
                .setVolume(volume)
                .setSide(side)
                .build()
        );
      } else {
        // ensure it was heart beat
        condition(primitive.isString() && primitive.getAsString().equals("hb"));
      }

    }

    return result;
  }
  // translators

  static String translate(Pair pair) {
    return pair.getBase().getSymbol() + pair.getQuote().getSymbol();
  }

  static Pair translate(String pair) {
    condition(pair.length() == 6);

    return Util.pair(pair.substring(0, 3), pair.substring(3, 6));
  }

  // stuff

  private static void checkForError(JsonObject json) {
    if (json.has("error")) {
      ExceptionUtils.wtf(json.toString());
    }
  }
}
