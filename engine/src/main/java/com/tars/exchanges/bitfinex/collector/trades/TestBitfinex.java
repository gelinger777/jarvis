package com.tars.exchanges.bitfinex.collector.trades;

import com.tars.util.concurrent.ConcurrencyUtils.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

import proto.Messages.Pair;
import com.tars.exchanges.bitfinex.BitfinexClient;
import com.tars.exchanges.bitfinex.collector.BitfinexContext;

import static com.tars.common.Util.pair;

class TestBitfinex {

  private static final Logger log = LoggerFactory.getLogger("trades-bitfinex");

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(BitfinexContext.class, args);

    BitfinexClient bitfinex = ctx.getBean(BitfinexClient.class);

    Pair pair = pair("BTC", "USD");

//    String path = absolutePathOf(bitfinex.config.getDataPath() + "/trades/" + folderNameFor(pair) + "/" + folderNameFor(pair));
//    StreamWriter writer = storage().writer(path);
//
//    bitfinex
//        .streamTrades(pair)
//        .observeOn(Schedulers.io())
//        .subscribe(trade -> {
//          log.info("trade : {} size {} bytes", asString(trade));
//          writer.append(trade.toByteArray());
//        });

    bitfinex
        .streamTrades(pair)
        .observeOn(Schedulers.io())
        .subscribe(
            order -> System.out.println("trade data"),
            throwable -> System.out.println("trade error"),
            () -> System.out.println("trade completion")
        );

    bitfinex
        .streamOrderbook(pair)
        .observeOn(Schedulers.io())
        .subscribe(
            order -> System.out.println("order data"),
            throwable -> System.out.println("order error"),
            () -> System.out.println("order completion")
        );

    System.out.println("enter to stop");
    new Scanner(System.in).nextLine();

    System.out.println("ACTIVE TRADE STREAMS");
    bitfinex.activeTradeStreams().forEach(System.out::println);

    System.out.println("ACTIVE ORDER STREAMS");
    bitfinex.activeOrderbookStreams().forEach(System.out::println);

    System.out.println("enter to stop");
    new Scanner(System.in).nextLine();

    bitfinex.closeTradeStream(pair);
    bitfinex.closeOrderbookStream(pair);

    System.out.println("enter to stop");
    new Scanner(System.in).nextLine();

    log.info("stopping bitfinex collector");
    ctx.close();


  }

  private static String folderNameFor(Pair pair) {
    return pair.getBase().getSymbol().toLowerCase() + "-" + pair.getQuote().getSymbol().toLowerCase();
  }
}
