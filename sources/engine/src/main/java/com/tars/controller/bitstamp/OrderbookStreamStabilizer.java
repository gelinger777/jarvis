//package com.tars.controller.bitstamp;
//
//class OrderbookStreamStabilizer {
//
////  private final static Logger log = LoggerFactory.getLogger(OrderbookStreamStabilizer.class);
////
////  private volatile boolean orderbookStreamInitialized = false;
////  private PublishSubject<Orderbook> subject = PublishSubject.create();
////  private LinkedList<Orderbook> buffer = new LinkedList<>();
////
////  OrderbookStreamStabilizer(Observable<Orderbook> original, Supplier<Option<Orderbook>> supplier) {
////    original
////        .subscribe(orderBook -> {
////          if (orderbookStreamInitialized) {
////            log.debug("already initialized, publishing");
////            subject.onNext(orderBook);
////          } else {
////            // if buffer is empty add and continue
////            if (buffer.isEmpty()) {
////              log.debug("nothing is buffered, adding the first");
////              buffer.add(orderBook);
////            } else {
////              log.debug("querying cached full orderbook");
////              // query last cached full orderbook
////              supplier.get().ifPresent(restOrderbook -> {
////
////                if (restOrderbook.getTime() > buffer.getFirst().getTime()) {
////
////                  log.debug("emitting initial orderbook");
////                  subject.onNext(restOrderbook);
////
////                  log.debug("consuming buffer");
////                  while (!buffer.isEmpty()) {
////                    Orderbook bufferedOrderbook = buffer.pollFirst();
////
////                    if (bufferedOrderbook.getTime() > restOrderbook.getTime()) {
////                      log.debug("emitting buffered data");
////                      subject.onNext(bufferedOrderbook);
////                    } else {
////                      log.debug("skipping an outdated data");
////                    }
////                  }
////
////                  log.debug("emitting current data (now synchronized)");
////                  subject.onNext(orderBook);
////                  orderbookStreamInitialized = true;
////
////                } else {
////                  log.debug("full orderbook still older, buffering this data");
////                  buffer.add(orderBook);
////                }
////              });
////            }
////          }
////        });
////  }
////
////  public Observable<Orderbook> stream() {
////    return subject;
////  }
//}
