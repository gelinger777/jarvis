package com.tars.exchanges.bitfinex.collector;

import java.io.File;
import java.util.Map;

import proto.Messages.Pair;

import static com.tars.util.Util.absolutePathOf;

public class BitfinexConfig {

  private String publicKey;
  private String privateKey;

  private String dataPath;

  private Map<String, Integer> trade;
  private Map<String, Integer> book;

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getDataPath() {
    return dataPath;
  }

  public void setDataPath(String dataPath) {
    this.dataPath = dataPath;
  }

  public Map<String, Integer> getBook() {
    return book;
  }

  public void setBook(Map<String, Integer> book) {
    this.book = book;
  }

  public Map<String, Integer> getTrade() {
    return trade;
  }

  public void setTrade(Map<String, Integer> trade) {
    this.trade = trade;
  }

  public String tradeDataPath(Pair pair) {
    String pairName = folderNameFor(pair);

    // ..\btc-usd\trades\data.*

    return absolutePathOf(
        getDataPath() + File.separator +
        pairName + File.separator +
        "trades" + File.separator +
        "data"
    );
  }

  public String bookDataPath(Pair pair) {
    String pairName = folderNameFor(pair);

    // ..\btc-usd\book\data.*

    return absolutePathOf(
        getDataPath() + File.separator +
        pairName + File.separator +
        "book" + File.separator +
        "data"
    );
  }

  private static String folderNameFor(Pair pair) {
    return pair.getBase().getSymbol().toLowerCase() + "-" + pair.getQuote().getSymbol().toLowerCase();
  }

}
