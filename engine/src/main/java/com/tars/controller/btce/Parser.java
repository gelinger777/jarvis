package com.tars.controller.btce;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import proto.Messages.Pair;
import proto.Messages.Trade;

import static com.tars.common.Util.pair;

/**
 * Converts bitstamp json messages to generic messages
 */
@SuppressWarnings("unchecked")
class Parser {

  // translators

  public static String translate(Pair pair) {
    return pair.getBase().getSymbol().toLowerCase() + "_" + pair.getQuote().getSymbol().toLowerCase();
  }

  // parsers

  public static Pair parsePair(String key) {
    String[] split = key.toUpperCase().split("_");
    return pair(split[0], split[1]);
  }

  public static List<Trade> parseTrades(String json) {

    JsonArray tradeArray =
        ((Entry<String, JsonElement>) new JsonParser().parse(json)
            .getAsJsonObject().entrySet().toArray()[0])
            .getValue()
            .getAsJsonArray();

    List<Trade> trades = new ArrayList<>();

    for (JsonElement jsonElement : tradeArray) {
      JsonObject trade = jsonElement.getAsJsonObject();

      trades.add(
          Trade.newBuilder()
//              .setId(trade.get("tid").getAsLong())
              .setTime(trade.get("timestamp").getAsLong() * 1000)
              .setPrice(trade.get("price").getAsDouble())
              .setVolume(trade.get("amount").getAsDouble())
              .build()
      );
    }
    return trades;
  }


}
