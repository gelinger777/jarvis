package sandbox;

import com.tars.util.concurrent.ConcurrencyUtils;
import com.tars.util.health.Pulse;

import org.junit.BeforeClass;
import org.junit.Test;

import static com.tars.util.concurrent.ConcurrencyUtils.bearSleep;

public class HearBeatWatchdogTest {

  @BeforeClass
  public static void init() {
    ConcurrencyUtils.init();
  }

  @Test(expected = IllegalStateException.class)
  public void testNotRegistered() throws Exception {
    Pulse.of("test");
  }

  @Test
  public void testNormal() throws Exception {
    Pulse pulse = Pulse.of("test", 2000, () -> System.err.println("heartbeat is fucked")).start();

    for (int i = 0; i < 3; i++) {
      pulse.beat();
      bearSleep(1500);
    }

//    pulse.stop();

    bearSleep(3000);
  }
}