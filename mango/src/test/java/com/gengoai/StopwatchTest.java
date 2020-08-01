package com.gengoai;

import com.gengoai.concurrent.Threads;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class StopwatchTest {

   @Test
   public void test() {
      Stopwatch sw = Stopwatch.createStarted("test1");
      Stopwatch sw2 = Stopwatch.createStarted();
      Threads.sleep(1, TimeUnit.SECONDS);
      assertEquals(1.0d, Math.round(sw.elapsed(TimeUnit.SECONDS)), 0);
      sw.stop();
      Threads.sleep(2, TimeUnit.SECONDS);
      assertEquals(3.0d, Math.round(sw2.elapsed(TimeUnit.SECONDS)), 0.5);

      assertTrue(sw.toString().startsWith("test1"));
   }
}