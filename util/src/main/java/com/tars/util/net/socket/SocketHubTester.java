package com.tars.util.net.socket;

import java.util.Scanner;

import rx.Subscription;

@SuppressWarnings("ALL")
class SocketHubTester {

  public static void main(String[] args) {
    // init
    SocketHub hub = new SocketHub();

    // define server

    SocketServer server = hub.server("localhost", 8090);

    server
        .stream()
        .subscribe(
            client -> {
              System.out.println("server : accepted new client");
              client.stream()
                  .subscribe(
                      bytes -> {
                        System.out.println("server.client got : " + new String(bytes));
                        client.send("echo".getBytes());
                      },
                      throwable -> System.out.println("server.client : got exception"),
                      () -> System.out.println("server.client : completed")
                  );
            }
        );

//    server.start();

    // define client

    SocketClient client = hub.client("localhost", 8090);

    Subscription subscription = client
        .stream()
        .subscribe(
            bytes -> System.out.println("client got : " + new String(bytes)),
            throwable -> System.out.println("client : got exception"),
            () -> System.out.println("client : completed")
        );

//    client.start();

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
        case "client unsub":
          subscription.unsubscribe();
          break;
        case "close":
          keepTaking = false;
          break;
        default:
          client.send(input.getBytes());
      }
    }

    client.stop();
    server.stop();
    hub.release();
  }
}
