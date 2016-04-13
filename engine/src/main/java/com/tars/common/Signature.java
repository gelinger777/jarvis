package com.tars.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.tars.util.exceptions.ExceptionUtils.executeAndGetMandatory;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class Signature {

  protected final String publicKey;
  protected final ThreadLocal<Mac> threadLocalMac;
  public Signature(Encoding encoding, String publicKey, String privateKey) {
    this.publicKey = publicKey;

    threadLocalMac = ThreadLocal.withInitial(
        () -> executeAndGetMandatory(() -> {
          Mac mac = Mac.getInstance(encoding.toString());
          mac.init(new SecretKeySpec(privateKey.getBytes(UTF_8), encoding.toString()));
          return mac;
        })
    );
  }

  protected byte[] sign(String... params) {
    Mac mac = threadLocalMac.get();

    for (String param : params) {
      mac.update(param.getBytes(UTF_8));
    }

    return mac.doFinal();
  }


  public enum Encoding {
    HMAC_SHA_512("HmacSHA512"),
    HMAC_SHA_384("HmacSHA384"),
    HMAC_SHA_256("HmacSHA256"),
    HMAC_SHA_1("HmacSHA1");

    private final String name;

    Encoding(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
