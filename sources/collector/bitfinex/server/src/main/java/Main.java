import java.net.*;

public class Main {

  public static void main(String[] args) throws MalformedURLException {

    ClassLoader cl1 = Thread.currentThread().getContextClassLoader();

    URLClassLoader
        cl2 =
        URLClassLoader
            .newInstance(new URL[]{new URL("file:/Users/vach/workspace/jarvis/gradle/wrapper/gradle-wrapper.jar")});

    System.out.println("cl: " + Thread.currentThread().getContextClassLoader());

    Thread.currentThread().setContextClassLoader(cl2);

    System.out.println("cl: " + Thread.currentThread().getContextClassLoader());

    Thread.currentThread().setContextClassLoader(cl1);

    System.out.println("cl: " + Thread.currentThread().getContextClassLoader());

  }
}
