package com.tars.controller.btce;


import com.tars.common.Signature;

import org.apache.http.client.methods.RequestBuilder;

import static com.tars.util.Util.bytesToHex;
import static com.tars.util.Util.unixTime;

public class BtceSignature extends Signature {

  public BtceSignature(String publicKey, String privateKey) {
    super(Encoding.HMAC_SHA_384, publicKey, privateKey);
  }

  RequestBuilder sign(RequestBuilder post) {
    String nonce = String.valueOf(unixTime());

    return post
        .addParameter("key", publicKey)
        .addParameter("signature", bytesToHex(sign(nonce, publicKey)).toUpperCase())
        .addParameter("nonce", nonce);
  }

}