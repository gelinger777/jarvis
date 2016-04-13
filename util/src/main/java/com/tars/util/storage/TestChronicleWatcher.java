package com.tars.util.storage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

import rx.Observable;
import rx.Subscription;

import static com.tars.util.Util.absolutePathOf;

final class TestChronicleWatcher {

  public static void main(String[] args) {
    Watcher watcher = Watchers.get(absolutePathOf("data/temp"));

    Observable<byte[]> watch = watcher.stream();
    LinkedList<Subscription> subscriptions = new LinkedList<>();

    Scanner scanner = new Scanner(System.in);
    boolean keepTaking = true;

    while (keepTaking) {
      System.out.println("awaiting for input");
      String input = scanner.nextLine();

      switch (input) {
        case "sub":
          if (watch != null) {
            subscriptions.add(
                watch
                    .subscribe(
                        bytes -> System.out.println(Arrays.toString(bytes)),
                        Throwable::printStackTrace,
                        () -> System.out.println("completed")
                    )
            );
          }
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
}
