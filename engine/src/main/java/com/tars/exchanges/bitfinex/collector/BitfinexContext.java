package com.tars.exchanges.bitfinex.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import exchange.bitfinex.Config;
import com.tars.exchanges.bitfinex.BitfinexClient;
import com.tars.util.concurrent.ConcurrencyUtils;
import com.tars.util.exceptions.ExceptionUtils;
import com.tars.util.net.NetworkUtils;
import com.tars.util.net.messenger.Mailer;

import static com.tars.util.exceptions.ExceptionUtils.stackTraceAsString;

@SpringBootApplication
public class BitfinexContext {

  private static final Logger log = LoggerFactory.getLogger(BitfinexContext.class);

  @Autowired
  private Environment environment;

  @Bean
  @ConfigurationProperties(prefix = "bitfinex")
  public Config config() {
    return new Config();
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  public BitfinexClient bitfinexClient(Config config) {
    return new BitfinexClient(config);
  }

  // lifecycle

  @PostConstruct
  public void init() {
    ConcurrencyUtils.init();
    NetworkUtils.init();

    // if in production report failures
    for (String profile : environment.getActiveProfiles()) {
      log.info("registering failure handler (production mode)");
      if (profile.equals("prod")) {
        ExceptionUtils.onUnrecoverableFailure(
            throwable -> Mailer.alert("unrecoverable error", stackTraceAsString(throwable))
        );
        break;
      }
    }
  }

  @PreDestroy
  public void destroy() {
    NetworkUtils.close();
    ConcurrencyUtils.close();
  }


}
