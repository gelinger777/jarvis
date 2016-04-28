//package com.tars.controller.bitstamp;
//
//import com.google.gson.*;
//
//import com.tars.util.exceptions.*;
//
//import proto.common.*;
//
//import static com.tars.controller.bitstamp.BitstampClient.*;
//
///**
// * Converts bitstamp json messages to generic messages
// */
//class Parser {
//
//  // parsers
//
//  static Trade parseTrade(String data) {
//
//    // {
//    //   "price": 329.92000000000002,
//    //     "amount": 0.75588021000000005,
//    //     "id": 9833618
//    // }
//
//    log.debug("parsing a trade");
//
//    JsonObject json = new JsonParser().parse(data).getAsJsonObject();
//
//    checkForError(json);
//
//    return Trade.newBuilder()
//        .setPrice(json.get("price").getAsDouble())
//        .setVolume(json.get("amount").getAsDouble())
////        .setId(json.get("id").getAsLong())
//        .build();
//  }
//
////  static Orderbook parseOrderbook(String data) {
////
////    // {
////    //   "timestamp": "1447318761",
////    //   "bids": [
////    //     [
////    //       "322.88",
////    //       "0"
////    //     ]
////    //   ],
////    //   "asks": [
////    //     [
////    //       "334.02",
////    //       "0"
////    //     ],
////    //     [
////    //       "334.26",
////    //       "1.10000000"
////    //     ],
////    //     [
////    //       "335.55",
////    //       "72.02000000"
////    //     ]
////    //   ]
////    // }
////
////    log.debug("parsing an orderbook");
////
////    JsonObject json = new JsonParser().parse(data).getAsJsonObject();
////
////    checkForError(json);
////
////    Builder builder = Orderbook.newBuilder();
//////        .setTime(json.get("timestamp").getAsLong());
////
////    for (JsonElement elem : json.get("bids").getAsJsonArray()) {
////      JsonArray orderArray = elem.getAsJsonArray();
////      builder.addBids(
////          Order.newBuilder()
////              .setPrice(orderArray.get(0).getAsDouble())
////              .setVolume(orderArray.get(1).getAsDouble())
////              .build()
////      );
////    }
////
////    for (JsonElement elem : json.get("asks").getAsJsonArray()) {
////      JsonArray orderArray = elem.getAsJsonArray();
////      builder.addAsks(
////          Order.newBuilder()
////              .setPrice(orderArray.get(0).getAsDouble())
////              .setVolume(orderArray.get(1).getAsDouble())
////              .build()
////      );
////    }
////
////    return builder.build();
////  }
//
//  static Order parseOrder(String data) {
//
//    // {
//    //   "price": "100.0",
//    //   "amount": "1.0",
//    //   "type": 0,
//    //   "id": 92830855,
//    //   "datetime": "2015-11-12 13:15:58.135738"
//    // }
//
//    log.debug("parsing an order");
//
//    JsonObject json = new JsonParser().parse(data).getAsJsonObject();
//
//    checkForError(json);
//
//    return Order.newBuilder()
//        .setPrice(json.get("price").getAsDouble())
//        .setVolume(json.get("amount").getAsDouble())
//        .setSide((json.get("type").getAsInt() == 0) ? Order.Side.BID : Order.Side.ASK)
////        .setId(json.get("id").getAsLong())
////        .setMetadata("bitstamp")
//        .build();
//  }
//
//  // stuff
//
//  private static void checkForError(JsonObject json) {
//    if (json.has("error")) {
//      // {"error":{"__all__":["You have only 0.0 BTC available. Check your account balance for details."]}}
//      ExceptionUtils.wtf(json.get("error").getAsJsonObject().get("__all__").getAsString());
//    }
//  }
//
//
//}
