package com.tars.util.net.http;

import com.tars.util.Option;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.tars.util.exceptions.ExceptionUtils.executeAndGetSilent;
import static com.tars.util.exceptions.ExceptionUtils.wtf;

/**
 * HttpHub provides interface for interacting with http protocol.
 */
public final class HttpHub {

  private static final Logger log = LoggerFactory.getLogger(HttpHub.class);
  private CloseableHttpClient hc;

  // lifecycle

  public HttpHub(){
    log.debug("initializing");
    hc = HttpClients.createDefault();
    log.info("initialized");
  }

  public void release() {
    log.debug("closing Http Client");
    try {
      hc.close();
    } catch (IOException e) {
      wtf(e);
    }
    log.info("released");
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
