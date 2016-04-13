package com.tars.util.storage;


import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import static com.tars.util.Util.absolutePathOf;
import static com.tars.util.concurrent.ConcurrencyUtils.bearSleep;


final class TestChronicleConsole {

  public static void main(String[] args) throws IOException {

    Chronicle chronicle = ChronicleQueueBuilder
        .indexed(absolutePathOf("data/temp"))
        .small()
        .build();

    Scanner scanner = new Scanner(System.in);
    boolean keepTaking = true;

    while (keepTaking) {
      System.out.println("awaiting for input");
      String input = scanner.nextLine();

      switch (input) {
        case "start":
          startThread(chronicle);
          break;
        case "stop":
          flag = false;
          break;
        case "append":
          try (ExcerptAppender appender = chronicle.createAppender()) {
            byte[] bytes = {0, 1, 0, 1, 0};
            int length = bytes.length;
            appender.startExcerpt(100);
            appender.writeInt(length);
            appender.write(bytes);
            appender.finish();
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        case "print":
          try (ExcerptTailer tailer = chronicle.createTailer()) {
            while (true) {
              if (tailer.nextIndex()) {
                byte[] data = new byte[tailer.readInt()];
                int bytesRead = tailer.read(data);
                tailer.finish();
                System.out.println(tailer.index() + " : " + bytesRead + " : " + Arrays.toString(data));
              } else {
                System.out.println("no more data");
                break;
              }
            }
          }
          break;

        case "size":
          System.out.println(chronicle.lastIndex());
          break;
        case "close":
          flag = false;
          keepTaking = false;
          break;
        default:
          System.out.println("unknown input");
      }
    }

    chronicle.close();
  }

  volatile static boolean flag = true;

  private static void startThread(Chronicle chronicle) {
    new Thread(() -> {

      try (ExcerptAppender appender = chronicle.createAppender()) {
        byte[] bytes = {0, 1, 0, 1, 0};
        int length = bytes.length;
        flag = true;
        while (flag) {
          bearSleep(1000);
          appender.startExcerpt(100);
          appender.writeInt(length);
          appender.write(bytes);
          appender.finish();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    }).start();
  }
}
