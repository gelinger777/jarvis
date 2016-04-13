package com.tars.controller.bitstamp;

import com.tars.common.GenericApiClient;
import com.tars.common.ProtoBufUtil;
import com.tars.util.Option;
import com.tars.util.concurrent.ConcurrencyUtils.Schedulers;
import com.tars.util.exceptions.ExceptionUtils;
import com.tars.util.net.NetworkUtils;
import com.tars.util.net.pusher.PusherHub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import proto.Messages.Order;
import proto.Messages.Pair;
import proto.Messages.Trade;
import rx.Observable;

import static com.tars.util.exceptions.ExceptionUtils.notImplemented;
import static org.apache.http.client.methods.RequestBuilder.post;

public class BitstampClient implements GenericApiClient {

  final static Logger log = LoggerFactory.getLogger(BitstampClient.class);

  private final BitstampSignature signature;
  private Map<Pair, Observable<Trade>> tradeStreams = new HashMap<>();
  private Map<Pair, Observable<Order>> orderBookStreams = new HashMap<>();

  public BitstampClient(BitstampConfig config) {
    this.signature = new BitstampSignature(
        config.getCustomerId(),
        config.getPublicKey(),
        config.getPrivateKey()
    );
  }

  // lifecycle

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  // trade stream

  @Override
  public Observable<Trade> streamTrades(Pair pair) {
    log.debug("starting trade stream : {}", ProtoBufUtil.json(pair));
//    condition(supportedMarkets.contains(pair), "not supported market pair");

    return tradeStreams.computeIfAbsent(
        pair,
        key -> {
          Observable<Trade> stream = PusherHub
              .stream("de504dc5763aeef9ff52", "live_trades", "trade")
              .observeOn(Schedulers.io())
              .map(Parser::parseTrade);

          stream.subscribe(
              trade -> log.debug("trade emitted : {}", ProtoBufUtil.json(trade)),
              ExceptionUtils::report
          );

          return stream;
        }
    );
  }

  @Override
  public Set<Pair> activeTradeStreams() {
    return tradeStreams.keySet();
  }

  @Override
  public void closeTradeStream(Pair pair) {
    log.debug("closing trade stream : {}", ProtoBufUtil.json(pair));
//    condition(supportedMarkets.contains(pair), "not supported market pair");

    Option.ofNullable(tradeStreams.remove(pair))
        .ifPresent(stream -> PusherHub.close("de504dc5763aeef9ff52", "live_trades", "trade"));
  }

  // orderbook stream

  @Override
  public Observable<Order> streamOrderbook(Pair pair) {
    return notImplemented();
//    log.debug("starting orderbook stream : {}", asString(pair));
//    condition(supportedMarkets.contains(pair), "not supported market pair");
//
//    return orderBookStreams.computeIfAbsent(
//        pair,
//        marketPair -> new OrderbookStreamStabilizer(
//            PusherHub
//                .stream("de504dc5763aeef9ff52", "diff_order_book", "data")
//                .map(Parser::parseOrderbook),
//            () -> NetworkUtils.http()
//                .getString(get("https://www.bitstamp.net/api/order_book/"))
//                .map(Parser::parseOrderbook)
//
//        ).stream()
//    );
  }

  @Override
  public Set<Pair> activeOrderbookStreams() {
    return orderBookStreams.keySet();
  }

  @Override
  public void closeOrderbookStream(Pair pair) {
    log.debug("closing orderbook stream : {}", ProtoBufUtil.json(pair));
//    condition(supportedMarkets.contains(pair), "not supported market pair");

    Option.ofNullable(orderBookStreams.remove(pair))
        .ifPresent(stream -> PusherHub.close("de504dc5763aeef9ff52", "diff_order_book", "data"));
  }

  // account stream

//  @Override
//  public Observable<AccountActivity> streamAccountActivity() {
//    return notImplemented();
//  }
//
//  @Override
//  public void closeActivityStream() {
//    notImplemented();
//  }

  // actions

  @Override
  public Option<Order> bid(Pair pair, double amount, double price) {
    log.debug("closing orderbook stream : {}", ProtoBufUtil.json(pair));
//    condition(supportedMarkets.contains(pair), "not supported market pair");

    return NetworkUtils.http()
        .getString(
            signature.sign(
                post("https://www.bitstamp.net/api/buy/")
                    .addParameter("amount", String.valueOf(amount))
                    .addParameter("price", String.valueOf(price))
            )
        )
        .map(Parser::parseOrder);

  }

  @Override
  public Option<Order> ask(Pair pair, double amount, double price) {
    log.debug("closing orderbook stream : {}", ProtoBufUtil.json(pair));
//    condition(supportedMarkets.contains(pair), "not supported market pair");

    return NetworkUtils.http()
        .getString(
            signature.sign(
                post("https://www.bitstamp.net/api/sell/")
                    .addParameter("amount", String.valueOf(amount))
                    .addParameter("price", String.valueOf(price))
            )
        )
        .map(Parser::parseOrder);
  }

  @Override
  public Option<Boolean> cancel(Pair pair, String id) {
    return NetworkUtils.http()
        .getString(
            signature.sign(
                post("https://www.bitstamp.net/api/cancel_order/")
                    .addParameter("id", id)
            )
        ).map(Boolean::parseBoolean);
  }
}
