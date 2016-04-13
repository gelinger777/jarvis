package com.tars.controller.bitstamp;

import java.util.List;

public class BitstampConfig {

  private String customerId;
  private String publicKey;
  private String privateKey;
  private String dataPath;
  private List<String> markets;

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

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

  public List<String> getMarkets() {
    return markets;
  }

  public void setMarkets(List<String> markets) {
    this.markets = markets;
  }
}
