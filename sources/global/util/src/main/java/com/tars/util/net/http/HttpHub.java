package com.tars.util.net.http;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import util.Option;

import static util.global.ExceptionHandlingKt.executeAndGetSilent;
import static util.global.ExceptionHandlingKt.wtf;



/**
 * HttpHub provides interface for interacting with http protocol.
 */
public final class HttpHub {

  private static final Logger log = LoggerFactory.getLogger("http");
  private CloseableHttpClient hc;

  // lifecycle

  public HttpHub() {
    log.info("init");
    hc = HttpClients.createDefault();
  }

  public void release() {
    log.info("shutdown");
    try {
      hc.close();
    } catch (IOException e) {
      wtf(e);
    }
  }

  // interface

  public Option<String> getString(RequestBuilder requestBuilder) {
    HttpUriRequest request = requestBuilder.build();
    log.debug("requesting {}", request);

    return executeAndGetSilent(
        () -> EntityUtils.toString(hc.execute(request).getEntity())
    );
  }

  public Option<String> getString(HttpUriRequest request) {
    log.debug("requesting {}", request);

    return executeAndGetSilent(
        () -> EntityUtils.toString(hc.execute(request).getEntity())
    );
  }

}
