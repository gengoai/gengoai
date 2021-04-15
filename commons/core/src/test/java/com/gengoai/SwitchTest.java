package com.gengoai;

import com.gengoai.function.Switch;
import org.junit.Test;

import static com.gengoai.function.Switch.$switch;
import static com.gengoai.string.StringMatcher.matches;
import static junit.framework.TestCase.assertEquals;

/**
 * @author David B. Bracewell
 */
public class SwitchTest {


   @Test
   public void builderTest() {
      Switch<Integer> stringToNumber = $switch($ -> {
         $.when(matches("one", false), s -> 1);
         $.when(matches("two", false), s -> 2);
         $.when(matches("three", false), s -> 3);
         $.defaultValue(0);
      });

      assertEquals(1, stringToNumber.apply("one"), 0);
      assertEquals(1, stringToNumber.apply("ONE"), 0);
      assertEquals(2, stringToNumber.apply("two"), 0);
      assertEquals(3, stringToNumber.apply("three"), 0);
      assertEquals(0, stringToNumber.apply("four"), 0);
      assertEquals(0, stringToNumber.apply(null), 0);
   }


   @Test(expected = RuntimeException.class)
   public void noDefault() {
      Switch<Integer> stringToNumber = $switch($ -> {
         $.when(matches("one", false), s -> 1);
         $.when(matches("two", false), s -> 2);
         $.when(matches("three", false), s -> 3);
      });

      assertEquals(1, stringToNumber.apply("one"), 0);
      assertEquals(1, stringToNumber.apply("ONE"), 0);
      assertEquals(2, stringToNumber.apply("two"), 0);
      assertEquals(3, stringToNumber.apply("three"), 0);
      assertEquals(0, stringToNumber.apply("four"), 0);
      assertEquals(0, stringToNumber.apply(null), 0);
   }


}
