package com.tars.exchanges.bitfinex;


import org.apache.http.client.methods.RequestBuilder;

import com.tars.common.Signature;

import static com.tars.util.Util.bytesToHex;
import static com.tars.util.Util.unixTime;

// todo use composition move to kotlin
public class BitfinexSignature extends Signature {

  public BitfinexSignature(String publicKey, String privateKey) {
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