package com.tars.util.concurrent;

public class CallbackRegisterTest{

//  @Test
//  public void shallBeReusable() {
//
//    CallbackRegister register = new CallbackRegister();
//
//    new Thread(() -> {
//      bearSleep(2000);
//      register.fail(new IllegalStateException());
//    }).start();
//
//    register
//        .onFailure(cause -> info("ON FAILURE ON MAIN"))
//        .waitForResult();
//
//    new Thread(() -> {
//      bearSleep(2000);
//      register.success("Yay!");
//    }).start();
//
//    register
//        .onSuccess(results -> info("ON SUCCESS ON MAIN"))
//        .waitForResult();
//
//    new Thread(() -> {
//      bearSleep(2000);
//      register.success();
//    }).start();
//
//    register
//        .onSuccess(result -> info("ON SUCCESS ON EXECUTING THREAD"));
//
//    bearSleep(2100);
//  }
//
//
//  @Test
//  public void shallIgnoreIfNoSubscribers() {
//
//    CallbackRegister register = new CallbackRegister();
//
//    new Thread(() -> {
//      bearSleep(2000);
//      register.fail(new IllegalStateException());
//    }).start();
//
//    bearSleep(3000);
//
//  }
}