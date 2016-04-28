package com.tars.util.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable.Operator;
import rx.Scheduler;
import rx.Subscriber;

import static util.global.ValidationKt.*;
import static java.util.Collections.unmodifiableList;

/**
 * Operator for batching values while consumer is busy processing the previous batch...
 */
public final class BatchPerSubscriber<T> implements Operator<Collection<T>, T> {

  final Scheduler scheduler;

  public BatchPerSubscriber(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public Subscriber<? super T> call(Subscriber<? super Collection<T>> subscriber) {
    return new BatchingSubscriber<>(subscriber, scheduler.createWorker());
  }

  private static final class BatchingSubscriber<T> extends Subscriber<T> {

    final Subscriber<? super List<T>> subscriber;
    final Scheduler.Worker worker;

    boolean done;
    boolean processing;

    List<T> waitQueue;
    List<T> waitQueueView; // immutable view of the wait queue

    List<T> executionQueue;
    List<T> executionQueueView; // immutable view of the execution queue

    Throwable error;

    BatchingSubscriber(Subscriber<? super List<T>> subscriber, Scheduler.Worker worker) {
      this.subscriber = subscriber;
      this.worker = worker;

      this.waitQueue = new ArrayList<>();
      this.waitQueueView = unmodifiableList(waitQueue);

      this.executionQueue = new ArrayList<>();
      this.executionQueueView = unmodifiableList(executionQueue);

      subscriber.add(worker);
      subscriber.add(this);
    }

    @Override
    public synchronized void onNext(T value) {
      condition(!done, "violation of Rx contract");
      waitQueue.add(value);
      schedule();
    }

    @Override
    public synchronized void onError(Throwable cause) {
      condition(!done, "violation of Rx contract");
      error = cause;
      done = true;
      schedule();
    }

    @Override
    public synchronized void onCompleted() {
      condition(!done, "violation of Rx contract");
      done = true;
      schedule();
    }

    private void schedule() {
      if (!processing) {
        worker.schedule(this::process);
        processing = true;
      }
    }


    synchronized void process() {
      if (isUnsubscribed()) {
        return;
      }

      boolean done;
      Throwable error;
      List<T> data;
      List<T> dataView;

      // read and modify the shared state
      synchronized (this) {
        done = this.done;
        error = this.error;

        // swap queues (and their immutable views)
        data = waitQueue;
        waitQueue = executionQueue;
        executionQueue = data;

        dataView = waitQueueView;
        waitQueueView = executionQueueView;
        executionQueueView = dataView;
      }

      if (!dataView.isEmpty()) {
        // pass immutable representation of the current execution queue
        // while this is being executed new values are stored in wait queue
        subscriber.onNext(dataView);

        // clear after processing the queue
        data.clear();
      }

      if (done) {
        if (error != null) {
          subscriber.onError(error);
        } else {
          subscriber.onCompleted();
        }

        waitQueue = null;
        waitQueueView = null;
        executionQueue = null;
        executionQueueView = null;
      }

      synchronized (this) {
        processing = false;
      }
    }

    private void swapQueues() {
      List<T> temp = waitQueue;
      waitQueue = executionQueue;
      executionQueue = temp;

      temp = waitQueueView;
      waitQueueView = executionQueueView;
      executionQueueView = temp;
    }
  }

}