package com.tars.controller.bitstamp;


import com.tars.common.Signature;

import org.apache.http.client.methods.RequestBuilder;

import static com.tars.util.Util.bytesToHex;
import static com.tars.util.Util.unixTime;

public class BitstampSignature extends Signature {

  private final String customerId;

  public BitstampSignature(String customerId, String publicKey, String privateKey) {
    super(Encoding.HMAC_SHA_256, publicKey, privateKey);
    this.customerId = customerId;
  }

  RequestBuilder sign(RequestBuilder post) {
    String nonce = String.valueOf(unixTime());

    return post
        .addParameter("key", publicKey)
        .addParameter("signature", bytesToHex(sign(nonce, customerId, publicKey)).toUpperCase())
        .addParameter("nonce", nonce);
  }

}