package com.tars.util.misc;

import java.io.InputStream;
import java.util.Collection;
import java.util.Scanner;
import java.util.function.Function;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observable.Transformer;
import rx.Scheduler;
import rx.Subscriber;
import rx.observables.ConnectableObservable;

public class ObservableUtils {

  /**
   * Represents provided input stream as an observable of strings.
   */
  public static ConnectableObservable<String> streamFrom(InputStream stream, Function<String, Boolean> exitCondition) {

    return Observable.create(new OnSubscribe<String>() {

      @Override
      public void call(Subscriber<? super String> subscriber) {

        if (subscriber.isUnsubscribed()) {
          return;
        }

        Scanner scanner = new Scanner(stream);
        String line;
        while (!subscriber.isUnsubscribed()) {
          line = scanner.nextLine();
          if (exitCondition.apply(line)) {
            break;
          }
          subscriber.onNext(line);
        }

        subscriber.onCompleted();
      }
    }).publish();
  }

  /**
   * Transformer that creates an observable that batches elements per subscriber. For each subscriber a single worker
   * will be taken from scheduler, and source stream will be batched as necessary while that worker is processing
   * previous batch.
   */
  public static <T> Transformer<T, Collection<T>> batchPerSubscriber(Scheduler scheduler) {
    return source -> source.lift(new BatchOperator<>(scheduler));
  }
}
