package com.tars.util.net.http;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;
import org.slf4j.*;

import java.io.*;

import util.*;

import static com.tars.util.exceptions.ExceptionUtils.*;

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
