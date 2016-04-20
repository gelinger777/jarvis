package util.future;

import com.google.common.util.concurrent.*;

import java.util.concurrent.*;

import static com.google.common.util.concurrent.Futures.*;

public final class FutureConverter {

  /**
   * Converts Guavas {@link com.google.common.util.concurrent.ListenableFuture} to java 8 {@link
   * java.util.concurrent.CompletableFuture}.
   */
  public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
    CompletableFuture<T> completable = new CompletableListenableFuture<>(listenableFuture);

    addCallback(
        listenableFuture,
        new FutureCallback<T>() {
          @Override
          public void onSuccess(T result) {
            // redirect success from source listenable future to target completable future
            completable.complete(result);
          }

          @Override
          public void onFailure(Throwable throwable) {
            // redirect failure from source listenable future to target completable future
            completable.completeExceptionally(throwable);
          }
        },
        MoreExecutors.directExecutor()
    );

    return completable;
  }
}

final class CompletableListenableFuture<T> extends CompletableFuture<T> {

  private final ListenableFuture<T> listenableFuture;

  CompletableListenableFuture(ListenableFuture<T> listenableFuture) {
    this.listenableFuture = listenableFuture;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    // redirect cancellation from target completable future to source listenable future
    boolean result = listenableFuture.cancel(mayInterruptIfRunning);
    super.cancel(mayInterruptIfRunning);
    return result;
  }
}