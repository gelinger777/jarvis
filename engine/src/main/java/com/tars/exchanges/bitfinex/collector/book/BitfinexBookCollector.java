package com.tars.exchanges.bitfinex.collector.book;

import com.tars.util.concurrent.ConcurrencyUtils.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

import proto.Messages.Order;
import proto.Messages.Pair;
import com.tars.common.ProtoBufUtil;
import com.tars.exchanges.bitfinex.BitfinexClient;
import com.tars.exchanges.bitfinex.collector.BitfinexContext;
import com.tars.util.storage.EventStream;

import static com.tars.common.Util.pair;
import static com.tars.util.misc.ObservableUtils.batchPerSubscriber;

@Deprecated
public class BitfinexBookCollector {

  private static final Logger log = LoggerFactory.getLogger("book-bitfinex");


  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(BitfinexContext.class, args);

    BitfinexClient bitfinex = ctx.getBean(BitfinexClient.class);

    Pair pair = pair("BTC", "USD");

    EventStream bookStream = EventStream.get(
        bitfinex.config.bookDataPath(pair)
    );

    bitfinex
        .streamOrderbook(pair)
        .compose(batchPerSubscriber(Schedulers.io()))
        .subscribe(orders -> {

          for (Order order : orders) {
            bookStream.append(order.toByteArray());
          }
          snapshotIfNecessary();
        });

    log.info("enter to stop");
    new Scanner(System.in).nextLine();

    log.info("ACTIVE ORDER STREAMS");
    bitfinex.activeOrderbookStreams().stream().map(ProtoBufUtil::json).forEach(log::info);

    log.info("stopping");
    ctx.close();
  }


  private static void snapshotIfNecessary() {

  }
}
