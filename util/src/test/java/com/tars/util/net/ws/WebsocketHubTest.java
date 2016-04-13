package com.tars.util.net.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class WebsocketHubTest {

  private final static Logger log = LoggerFactory.getLogger(WebsocketHubTest.class);



  public static void main(String[] args) {
    WebsocketHub hub = new WebsocketHub();




    WebsocketServer server = hub.server(8089, "/test");

//    Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    server.stream()
//        .observeOn(scheduler)
        .subscribe(websocketClient -> {
          log.info("accepted a client");
          websocketClient.stream()
//              .observeOn(scheduler)
              .subscribe(
                  (message) -> {
                    log.info("server.client got : " + message);
                    websocketClient.send("echo from server");
                  },
                  (error) -> log.error("server.client got : " + error.getMessage()),
                  () -> log.info("server.client completed")
              );
        });

    WebsocketClient client = hub.client("ws://localhost:8089/test");

    client.stream()
//        .observeOn(scheduler)
        .subscribe(
            (message) -> log.info("client got : " + message),
            (error) -> log.error("client got : " + error.getMessage()),
            () -> log.info("client completed")
        );

    // interaction

    Scanner scanner = new Scanner(System.in);
    boolean keepTaking = true;

    while (keepTaking) {
      System.out.println("awaiting for input");
      String input = scanner.nextLine();

      switch (input) {
        case "client isAlive":
          System.out.println(client.isAlive());
          break;
        case "client stop":
          client.stop();
          break;
        case "client start":
          client.start();
          break;
        case "server isAlive":
          System.out.println(server.isAlive());
          break;
        case "server stop":
          server.stop();
          break;
        case "server start":
          server.start();
          break;
        case "close":
          keepTaking = false;
          break;
        default:
          client.send(input);
      }

    }
  }
}