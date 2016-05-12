//package com.tars.util.misc;
//
//import com.tars.util.concurrent.ConcurrencyUtils;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.util.Collection;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicLong;
//
//import rx.schedulers.Schedulers;
//import rx.schedulers.TestScheduler;
//import rx.subjects.PublishSubject;
//
//import static com.tars.util.concurrent.ConcurrencyUtils.bearSleep;
//import static java.lang.Thread.currentThread;
//
//public class ObservableUtilsTest {
//
//  @BeforeClass
//  public static void init() {
//    ConcurrencyUtils.init();
//  }
//
//  @AfterClass
//  public static void release() {
//    ConcurrencyUtils.close();
//  }
//
//
//  @Test
//  public void testConcurrent() {
//    PublishSubject<Integer> subject = PublishSubject.create();
//
//    AtomicLong count = new AtomicLong(0);
//
//    List<Collection> collections = new LinkedList<>();
//
//    subject
//        .compose(ObservableUtils.batchPerSubscriber(Schedulers.io()))
//        .subscribe(
//            (v) -> {
//              boolean isThere = false;
//              for (Collection collection : collections) {
//                if (collection == v) {
//                  isThere = true;
//                  break;
//                }
//              }
//
//              if (!isThere) {
//                collections.add(v);
//              }
//
//              System.out.println(currentThread().getName() + " : " + v.size());
//
//              count.addAndGet(v.size());
//            },
//            (e) -> System.err.println(currentThread().getName() + " : " + e),
//            () -> System.out.println("Done")
//        );
//
//    for (int i = 0; i < 200000; i++) {
//      subject.onNext(i);
//    }
//
//    subject.onCompleted();
//
////    Assert.assertEquals(2, lists.size());
////    Assert.assertEquals(100000, count.intValue());
//
//    bearSleep(1000);
//
//    System.out.println("lists used : " + collections.size());
//    System.out.println("count : " + count);
//  }
//
//  @Test
//  public void testSequential() {
//    PublishSubject<Integer> ps = PublishSubject.create();
//    TestScheduler sch = Schedulers.test();
//
//    ps.lift(new BatchOperator<>(sch))
//        .subscribe(
//            (value) -> System.out.println(currentThread().getName() + " : " + value),
//            Throwable::printStackTrace,
//            () -> System.out.println("Done")
//        );
//
//    ps.onNext(1);
//    ps.onNext(2);
//
//    sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);
//
//    ps.onNext(3);
//    ps.onNext(4);
//    ps.onNext(5);
//    ps.onCompleted();
//
//    sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);
//  }
//}