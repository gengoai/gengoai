package com.gengoai.collection.counter;

import java.util.Collections;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public class HashMapCounterTest extends BaseCounterTest {

   @Override
   protected Counter<String> getCounter1() {
      return Counters.newCounter("a", "b", "c", "a", "b", "a");
   }

   @Override
   protected Counter<String> getCounter2() {
      return Counters.newCounter(hashMapOf($("A", 4.0), $("B", 5.0), $("C", 1.0)));
   }

   @Override
   protected Counter<String> getCounter3() {
      return Counters.newCounter(Collections.singleton("a"));
   }

   @Override
   protected Counter<String> getEmptyCounter() {
      return Counters.newCounter();
   }


}//END OF HashMapCounterTest
