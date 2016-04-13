package com.tars.exchanges.bitfinex;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.tars.common.GenericApiClient;
import com.tars.common.ProtoBufUtil;
import com.tars.exchanges.bitfinex.Channel.Type;
import com.tars.exchanges.bitfinex.collector.BitfinexConfig;
import com.tars.util.Option;
import com.tars.util.concurrent.ConcurrencyUtils.Schedulers;
import com.tars.util.exceptions.ExceptionUtils;
import com.tars.util.health.Pulse;
import com.tars.util.net.NetworkUtils;
import com.tars.util.net.ws.WebsocketClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import exchange.bitfinex.Config;
import proto.Messages.Order;
import proto.Messages.Pair;
import proto.Messages.Trade;
import rx.Observable;

import static com.tars.common.ProtoBufUtil.tradeStreamName;
import static com.tars.exchanges.bitfinex.Parser.translate;
import static com.tars.util.Util.cast;
import static com.tars.util.Util.join;
import static com.tars.util.exceptions.ExceptionUtils.executeAndGetSilent;
import static com.tars.util.exceptions.ExceptionUtils.executeSilent;
import static com.tars.util.exceptions.ExceptionUtils.notImplemented;
import static com.tars.util.exceptions.ExceptionUtils.wtf;
import static com.tars.util.net.NetworkUtils.socket;
import static com.tars.util.net.messenger.Mailer.createEmailCallback;
import static com.tars.util.validation.Validator.condition;

public class BitfinexClient implements GenericApiClient {

  final static Logger log = LoggerFactory.getLogger(BitfinexClient.class);

  // configuration

  public final Config config;
//  private BitfinexSignature signature;

  // state

  private WebsocketClient wsClient;
  private Map<Pair, Channel<Trade>> tradeStreams = new HashMap<>();
  private Map<Pair, Channel<Order>> orderbookStreams = new HashMap<>();
  private Map<Integer, Channel> streamsByChannel = new HashMap<>();

  // lifecycle

  public BitfinexClient(Config config) {
    this.config = config;

    // make sure ports are accessible
//    checkPorts(config);

//    // create the signature
//    this.signature = new BitfinexSignature(
//        config.getPublicKey(),
//        config.getPrivateKey()
//    );

    log.info("initialized");
  }

  @Override
  public void start() {
    log.debug("starting");
    wsClient = NetworkUtils.websocket()
        .client("wss://api2.bitfinex.com:3000/ws")
        .start();

    wsClient.stream()
        .observeOn(Schedulers.io())
        .subscribe(
            this::handleMessage,
            ExceptionUtils::wtf
        );
    log.info("started");
  }


  @Override
  public void stop() {
    log.debug("stopping");
    executeSilent(() -> {
      activeOrderbookStreams().forEach(this::closeOrderbookStream);
      activeTradeStreams().forEach(this::closeTradeStream);

      wsClient.stop();
    });

    log.info("stopped");
  }

  // trading stream

  @Override
  public Observable<Trade> streamTrades(Pair pair) {
    log.info("starting trade stream : {}", ProtoBufUtil.json(pair));

    return tradeStreams
        .computeIfAbsent(pair, key -> {
          wsClient.send("{\"event\": \"subscribe\",\"channel\": \"trades\",\"pair\": \"" + translate(pair) + "\"}");

          // monitor this stream
          String name = tradeStreamName("BITFINEX", pair);

          Pulse.of(name, 10000, createEmailCallback(
              "issue with data stream",
              "no updates for 10 seconds for " + name)
          ).start();

          return new Channel<Trade>(Type.TRADE).pair(pair);
        })
        .stream();
  }

  @Override
  public Set<Pair> activeTradeStreams() {
    return tradeStreams.keySet();
  }

  @Override
  public void closeTradeStream(Pair pair) {
    log.info("closing trade stream : {}", ProtoBufUtil.json(pair));

    if (activeTradeStreams().contains(pair)) {
      wsClient.send("{\"event\":\"unsubscribe\",\"chanId\":\"" + tradeStreams.get(pair).chanId + "\"}");

      // disable monitoring
      Pulse.of(tradeStreamName("BITFINEX", pair)).ifPresent(Pulse::stop);
    }
  }

  // orderbook stream

  @Override
  public Observable<Order> streamOrderbook(Pair pair) {
    log.info("starting orderbook stream : {}", ProtoBufUtil.json(pair));

    return orderbookStreams
        .computeIfAbsent(pair, key -> {
          wsClient.send("{\"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"" + translate(pair)
                        + "\",\"prec\":\"R0\",\"len\":\"full\"}");
          return new Channel<Order>(Type.ORDERBOOK).pair(pair);
        })
        .stream();
  }

  @Override
  public Set<Pair> activeOrderbookStreams() {
    return orderbookStreams.keySet();
  }

  @Override
  public void closeOrderbookStream(Pair pair) {
    log.debug("closing orderbook stream : {}", ProtoBufUtil.json(pair));

    if (orderbookStreams.containsKey(pair)) {
      wsClient.send("{\"event\":\"unsubscribe\",\"chanId\":\"" + orderbookStreams.get(pair).chanId + "\"}");
    }
  }

//  // account stream
//
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
    return notImplemented();
  }

  @Override
  public Option<Order> ask(Pair pair, double amount, double price) {
    return notImplemented();
  }

  @Override
  public Option<Boolean> cancel(Pair pair, String id) {
    return notImplemented();
  }

  // stuff

  private void checkPorts(BitfinexConfig config) {
    for (Integer port : join(config.getTrade().values(), config.getBook().values())) {
      if (!socket().isPortAvailable(port)) {
        wtf("port %d is not available", port);
        break;
      }
    }
  }

  /**
   * Accepts all websocket messages from server.
   */
  private void handleMessage(String data) {
    log.debug("handling websocket message : {}", data);

    JsonElement rootElement = new JsonParser().parse(data);

    if (rootElement.isJsonObject()) {
      JsonObject rootObject = rootElement.getAsJsonObject();

      condition(rootObject.has("event"));

      switch (rootObject.get("event").getAsString()) {
        case "info": {
          condition(rootObject.get("version").getAsInt() == 1, "bitfinex api version mismatch");
          break;
        }
        case "pong": {
          log.debug("connection is fine with server");
          break;
        }
        case "subscribed": {
          int chanId = rootObject.get("chanId").getAsInt();
          Pair pair = translate(rootObject.get("pair").getAsString());

          switch (rootObject.get("channel").getAsString()) {
            case "trades":
              streamsByChannel.put(
                  chanId,
                  tradeStreams.get(pair).chanId(chanId)
              );
              break;
            case "book":
              streamsByChannel.put(
                  chanId,
                  orderbookStreams.get(pair).chanId(chanId)
              );
              break;
            default:
              wtf();
          }

          log.debug("registered channel : {} with id {}", ProtoBufUtil.json(pair), chanId);

          log.debug(
              "subscribed to {} : {}",
              rootObject.get("channel").getAsString(),
              rootObject.get("pair").getAsString()
          );
          break;
        }
        case "unsubscribed":
          int chanId = rootObject.get("chanId").getAsInt();

          Channel channel = streamsByChannel.remove(chanId);

          switch (channel.type) {
            case TRADE:
              tradeStreams.remove(channel.pair);
              break;
            case ORDERBOOK:
              orderbookStreams.remove(channel.pair);
              break;
          }

          channel.stream.onCompleted();

          log.debug("unsubscribed from {}", chanId);

          break;
        default: {
          wtf("unknown message from server : " + data);
        }
      }
    } else {

      JsonArray rootArray = rootElement.getAsJsonArray();

      Integer channelId = rootArray.get(0).getAsInt();

      Channel untypedStream = streamsByChannel.get(channelId);
      switch (untypedStream.type) {
        case TRADE:
          Channel<Trade> tradeChannel = cast(untypedStream);
          executeAndGetSilent(() -> Parser.parseTrade(tradeChannel, rootArray))
              .ifPresent(trade -> {
                log.debug("trade emitted : {}", ProtoBufUtil.json(trade));
                tradeChannel.stream().onNext(trade);
              });
          break;
        case ORDERBOOK:
          Channel<Order> orderChannel = cast(untypedStream);
          executeAndGetSilent(() -> Parser.parseOrderbook(rootArray))
              .ifPresent(book -> {
                for (Order order : book) {
//                  log.debug("orderbook emitted : {}", ProtoBufUtil.toString(order));
                  orderChannel.stream().onNext(order);
                }
              });
          break;
        default:
          wtf();
      }
    }

  }


}
