package com.tars.exchanges.bitfinex.collector.trades;

import com.tars.util.concurrent.ConcurrencyUtils.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

import proto.Messages.Pair;
import proto.Messages.Trade;
import com.tars.common.ProtoBufUtil;
import com.tars.exchanges.bitfinex.BitfinexClient;
import com.tars.exchanges.bitfinex.collector.BitfinexContext;
import com.tars.util.exceptions.ExceptionUtils;
import com.tars.util.net.messenger.Mailer;
import com.tars.util.storage.EventStream;

import static com.tars.common.Util.pair;
import static com.tars.util.exceptions.ExceptionUtils.stackTraceAsString;
import static com.tars.util.misc.ObservableUtils.batchPerSubscriber;

@Deprecated
public class BitfinexTradeCollector {

  private static final Logger log = LoggerFactory.getLogger("trades-bitfinex");

  public static void main(String[] args) {
    ExceptionUtils.onUnrecoverableFailure((throwable)->{
      Mailer.alert("unrecoverable error", stackTraceAsString(throwable));
    });

    ConfigurableApplicationContext ctx = SpringApplication.run(BitfinexContext.class, args);

    BitfinexClient bitfinex = ctx.getBean(BitfinexClient.class);

    Pair pair = pair("BTC", "USD");

    EventStream tradeStream = EventStream.get(
        bitfinex.config.tradeDataPath(pair)
    );

    bitfinex
        .streamTrades(pair)
        .compose(batchPerSubscriber(Schedulers.io()))
        .subscribe(trades -> {
          for (Trade trade : trades) {
            log.info("trade : {}", ProtoBufUtil.json(trade));
            tradeStream.append(trade.toByteArray());
          }
        });

    log.info("enter to stop");
    new Scanner(System.in).nextLine();

    log.info("ACTIVE TRADE STREAMS");
    bitfinex.activeTradeStreams().stream().map(ProtoBufUtil::json).forEach(log::info);

    log.info("stopping");
    ctx.close();
  }
}
