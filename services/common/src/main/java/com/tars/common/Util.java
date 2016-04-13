package com.tars.common;

import proto.Currency;
import proto.Pair;

import static com.tars.util.validation.Validator.allNotNullOrEmpty;
import static com.tars.util.validation.Validator.areUpperCase;
import static com.tars.util.validation.Validator.condition;

@Deprecated
public abstract class Util {

  // builders

  public static Pair pair(String base, String quote) {
    condition(allNotNullOrEmpty(base, quote) && areUpperCase(base, quote));

    return Pair.newBuilder()
        .setBase(Currency.newBuilder().setSymbol(base).build())
        .setQuote(Currency.newBuilder().setSymbol(quote).build())
        .build();
  }

  public static Pair pair(String id) {
    condition(allNotNullOrEmpty(id));

    String[] symbols = id.split("\\|");
    return pair(symbols[0], symbols[1]);
  }

  // test

//  public static void main(String[] args) {
//    System.out.println(pair("BTC|USD"));
//  }
}
