//package com.tars.util.net.streamer;
//
//import com.tars.util.concurrent.ConcurrencyUtils;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.util.concurrent.Callable;
//import java.util.concurrent.ThreadLocalRandom;
//
//import static com.tars.util.concurrent.ConcurrencyUtils.bearSleep;
//
//
//public class SingleThreadStreamerTest {
//
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
//  public void test() throws Exception {
//    Callable<Double> callable = () -> {
//      double value = ThreadLocalRandom.current().nextDouble();
//      if (value < 0.2) {
//        throw new IllegalStateException();
//      } else {
//        return value;
//      }
//    };
//
//    SingleThreadStreamer<Double> streamer = new SingleThreadStreamer<>(callable, 100);
//    streamer
//        .start()
//        .stream()
//        .subscribe(System.out::println);
//
//    bearSleep(20000);
//
//  }
//}