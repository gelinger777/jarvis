package com.tars.controller.bitstamp;

/**
 * Bitstamp exchange api client provides java api to all public and private functionality.
 *
 * If api returns error it will be thrown as a BitstampException.
 */
@Deprecated
class BitstampApiClientLegacy {
//  private static final Logger log = LoggerFactory.getLogger(BitstampApiClient.class);
//  BitstampSignature signature;
//
//
//  public BitstampApiClient(BitstampSignature signature) {
//    this.signature = signature;
//  }
//
//  public Option<DayTicker> dayTicker() {
//    try {
//      return NetworkUtils.http().getString(get("https://www.bitstamp.net/api/ticker/"))
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.DayTicker", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<HourTicker> hourTicker() {
//    try {
//      return NetworkUtils.http().getString(get("https://www.bitstamp.net/api/ticker_hour/"))
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.HourTicker", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<OrderBook> orderBook() {
//    try {
//      return NetworkUtils.http().getString(get("https://www.bitstamp.net/api/order_book/"))
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.OrderBook", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<Transactions> transactions() {
//    try {
//      return NetworkUtils.http().getString(get("https://www.bitstamp.net/api/transactions/"))
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.Transactions", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  // private api
//
//  public Option<Balance> balance() {
//    try {
//      return NetworkUtils.http()
//          .getString(signature.sign(post("https://www.bitstamp.net/api/balance/")))
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.Balance", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<UserTransactions> userTransactions(Integer offset, Integer limit, Boolean orderAsc) {
//    try {
//      RequestBuilder post = post("https://www.bitstamp.net/api/user_transactions/");
//
//      if (notNull(offset)) {
//        post.addParameter("offset", offset.toString());
//      }
//
//      if (notNull(limit) && limit < 1000) {
//        post.addParameter("limit", limit.toString());
//      }
//
//      if (notNull(orderAsc)) {
//        post.addParameter("sort", (orderAsc) ? "asc" : "desc");
//      }
//
//      return NetworkUtils.http().getString(signature.sign(post))
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.UserTransactions", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<UserOrders> openOrders() {
//    try {
//      return NetworkUtils.http().getString(signature.sign(post("https://www.bitstamp.net/api/open_orders/")))
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.UserOrders", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<OrderStatus> orderStatus(long id) {
//    try {
//      return NetworkUtils.http()
//          .getString(
//              signature.sign(
//                  post("https://www.bitstamp.net/api/order_status/")
//                      .addParameter("id", String.valueOf(id))
//              )
//          )
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.OrderStatus", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<Boolean> cancelOrder(long id) {
//    try {
//      return NetworkUtils.http()
//          .getString(
//              signature.sign(
//                  post("https://www.bitstamp.net/api/cancel_order/")
//                      .addParameter("id", String.valueOf(id))
//              )
//          )
//          .map(BitstampApiClient::parseBoolean);
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<Boolean> cancelAllOrders() {
//    try {
//      return NetworkUtils.http().getString(signature.sign(post("https://www.bitstamp.net/api/cancel_all_orders/")))
//          .map(BitstampApiClient::parseBoolean);
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<UserOrder> buy(double amount, double price) {
//    try {
//      return NetworkUtils.http()
//          .getString(
//              signature.sign(
//                  post("https://www.bitstamp.net/api/buy/")
//                      .addParameter("amount", String.valueOf(amount))
//                      .addParameter("price", String.valueOf(price))
//              )
//          )
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.UserOrder", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  public Option<UserOrder> sell(double amount, double price) {
//    try {
//      return NetworkUtils.http()
//          .getString(
//              signature.sign(
//                  post("https://www.bitstamp.net/api/sell/")
//                      .addParameter("amount", String.valueOf(amount))
//                      .addParameter("price", String.valueOf(price))
//              )
//          )
//          .flatMap(response -> ProtobufDecoder.decode("bitstamp.UserOrder", response));
//    } catch (RuntimeException cause) {
//      throw wrapExceptionIfApplicable(cause);
//    }
//  }
//
//  // streaming api
//
//  public Observable<Trade> streamTrades() {
//    return PusherHub.stream("de504dc5763aeef9ff52", "live_trades", "trade")
//        .map(data -> ProtobufDecoder.<Trade>decode("bitstamp.Trade", data))
//        .filter(Option::isPresent)
//        .map(Option::get);
//  }
//
//  public Observable<String> streamTradesRaw() {
//    return PusherHub.stream("de504dc5763aeef9ff52", "live_trades", "trade");
//  }
//
//  private volatile boolean orderbookStreamInitialized = false;
//  private PublishSubject<OrderBook> subject;
//
//
//  public synchronized Observable<OrderBook> streamOrderBook() {
//
//    if (orderbookStreamInitialized) {
//      return subject;
//    }
//
//    subject = PublishSubject.create();
//    LinkedList<OrderBook> streamBuffer = new LinkedList<>();
//
//    PusherHub.stream("de504dc5763aeef9ff52", "diff_order_book", "data")
//        .map(data -> ProtobufDecoder.<OrderBook>decode("bitstamp.OrderBook", data))
//        .filter(Option::isPresent)
//        .map(Option::get)
//        .subscribe(orderBook -> {
//          if (orderbookStreamInitialized) {
//            log.debug("already initialized, publishing");
//            subject.onNext(orderBook);
//          } else {
//            // if buffer is empty add and continue
//            if (streamBuffer.isEmpty()) {
//              log.debug("nothing is buffered, adding the first");
//              streamBuffer.add(orderBook);
//            } else {
//              // take oldest timestamp
//              long firstBufferedOrderbookTime = parseLong(streamBuffer.getFirst().getTimestamp());
//
//              log.debug("querying cached full orderbook");
//              // query last cached full orderbook
//              orderBook().ifPresent(restOrderbook -> {
//
//                // if timestamp is newer then apply
//                long fullOrderbookTimestamp = parseLong(restOrderbook.getTimestamp());
//
//                if (fullOrderbookTimestamp > firstBufferedOrderbookTime) {
//
//                  log.debug("emitting initial orderbook");
//                  subject.onNext(restOrderbook);
//
//                  log.debug("consuming buffer");
//                  while (!streamBuffer.isEmpty()) {
//                    OrderBook bufferedOrderbook = streamBuffer.pollFirst();
//
//                    if (parseLong(bufferedOrderbook.getTimestamp()) > fullOrderbookTimestamp) {
//                      log.debug("emitting buffered data");
//                      subject.onNext(bufferedOrderbook);
//                    } else {
//                      log.debug("skipping an outdated data");
//                    }
//                  }
//
//                  log.debug("emitting current data (now synchronized)");
//                  subject.onNext(orderBook);
//                  orderbookStreamInitialized = true;
//                } else {
//                  log.debug("full orderbook still older, buffering this data");
//                  streamBuffer.add(orderBook);
//                }
//
//              });
//            }
//          }
//        });
//
//    return subject;
//  }
//
//  // stuff
//
//  private static Boolean parseBoolean(String value) {
//    if (value == null) {
//      return null;
//    }
//
//    switch (value) {
//      case "true":
//        return true;
//      case "false":
//        return false;
//      default:
//        return null;
//    }
//  }
//
//  private static RuntimeException wrapExceptionIfApplicable(RuntimeException cause) {
//    return Option.ofNullable(cause.getMessage())
//        .filter(message -> message.contains("\"error\""))
//        .map(BitstampException::new)
//        .ifNotPresent(cause)
//        .get();
//  }
//
//  public static class BitstampException extends RuntimeException {
//
//    public BitstampException(String message) {
//      super(message);
//    }
//  }
}
