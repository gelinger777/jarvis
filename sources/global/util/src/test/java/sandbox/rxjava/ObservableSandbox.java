//package sandbox.rxjava;
//
//import org.junit.Test;
//
//import java.util.concurrent.TimeUnit;
//
//import rx.Observable;
//import rx.subjects.PublishSubject;
//
//import static com.tars.util.concurrent.ConcurrencyUtils.bearSleep;
//import static util.Validator.notNullOrEmpty;
//
//public class ObservableSandbox {
//
//
//  @Test
//  public void subject() {
//    PublishSubject<String> subject = PublishSubject.create();
//
//    Observable<String> observable = subject;
//
//    observable.subscribe(msg -> {
//      bearSleep(500);
//      log("a : " + msg);
//    });
//
//    observable.subscribe(msg -> {
//      bearSleep(500);
//      log("b : " + msg);
//    });
//
//    observable.
//
//        subscribe(msg -> {
//      bearSleep(500);
//      log("c : " + msg);
//    });
//
//    subject.onNext("mi urish ban");
//
//  }
//
//  public static void log(String msg) {
//    assert notNullOrEmpty(msg);
//    System.out.printf("%s : %s\n", Thread.currentThread().getName(), msg);
//  }
//
//  @Test
//  public void zip() {
//    Observable<String> data1 = Observable.just("one", "two", "three", "four", "five");
//    Observable<Long> interval = Observable.interval(1, TimeUnit.SECONDS);
//    Observable<Long> interval2 = Observable.interval(1, TimeUnit.SECONDS);
//
//    Observable.zip(data1, interval, interval2, (d, i, i2) -> d + " " + i + " " + i2).subscribe(System.out::println);
//
//    bearSleep(10000);
//  }
//}
