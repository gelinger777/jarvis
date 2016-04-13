package com.tars.util.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Scanner;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.tars.util.Util.absolutePathOf;

final class TestEventStream {

  static Logger logger = LoggerFactory.getLogger(TestEventStream.class);

  public static void main(String[] args) {
    EventStreamImpl es = new EventStreamImpl(absolutePathOf("data/temp"));

    LinkedList<Subscription> subscriptions = new LinkedList<>();

    Scanner scanner = new Scanner(System.in);
    boolean keepTaking = true;

    while (keepTaking) {
      System.out.println("awaiting for input");
      String input = scanner.nextLine();

      switch (input) {
        case "stream":
          subscriptions.add(
              subscribe(es.stream())
          );
          break;

        case "from":
          subscriptions.add(
              subscribe(es.streamFrom(5))
          );
          break;

        case "fromto":
          subscriptions.add(
              subscribe(es.streamFromTo(5, 10))
          );
          break;

        case "all":
          subscriptions.add(
              subscribe(es.streamFromStart())
          );
          break;

        case "size":
          System.out.println(es.size());
          break;

        case "unsub":
          subscriptions.poll().unsubscribe();
          break;

        case "close":
          keepTaking = false;
          break;
        default:
          System.out.println("unknown input");
      }
    }
  }

  static int i = 0;

  private static Subscription subscribe(Observable<byte[]> observable) {
    return observable
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .subscribe(
            bytes -> logger.debug("" + i++),
            Throwable::printStackTrace,
            () -> logger.info("completed")
        );
  }
}
