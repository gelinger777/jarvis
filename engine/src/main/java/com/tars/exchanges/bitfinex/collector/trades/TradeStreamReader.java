package com.tars.exchanges.bitfinex.collector.trades;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import proto.Messages.Trade;
import rx.schedulers.Schedulers;
import com.tars.common.ProtoBufUtil;
import com.tars.util.storage.EventStream;

import static com.tars.util.misc.ObservableUtils.batchPerSubscriber;

public class TradeStreamReader {

  public static void main(String[] args) {
    EventStream
        ts =
        EventStream.get("/Users/vach/workspace/projects/project-tars/data/dev/bitfinex/btc-usd/trades/data");

    AtomicInteger count = new AtomicInteger(0);

    ts.streamFromStart()
        .compose(batchPerSubscriber(Schedulers.io()))
        .subscribe(trades -> {
          for (byte[] data : trades) {
            Trade trade = null;
            try {
              trade = Trade.parseFrom(data);
            } catch (InvalidProtocolBufferException e) {
              e.printStackTrace();
            }
            System.out.println(count.incrementAndGet() + " : " + ProtoBufUtil.json(trade));
          }
        });

    System.out.println("enter to stop");
    new Scanner(System.in).nextLine();
    System.out.println("TOTAL : " + count);
  }

}
