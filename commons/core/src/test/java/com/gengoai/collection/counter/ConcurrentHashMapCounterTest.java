package com.gengoai.collection.counter;

import java.util.Collections;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public class ConcurrentHashMapCounterTest extends BaseCounterTest {

   @Override
   protected Counter<String> getCounter1() {
      return Counters.newConcurrentCounter("a", "b", "c", "a", "b", "a");
   }

   @Override
   protected Counter<String> getCounter2() {
      return Counters.newConcurrentCounter(hashMapOf($("A", 4.0), $("B", 5.0), $("C", 1.0)));
   }

   @Override
   protected Counter<String> getCounter3() {
      return Counters.newConcurrentCounter(Collections.singleton("a"));
   }

   @Override
   protected Counter<String> getEmptyCounter() {
      return Counters.newConcurrentCounter();
   }


}//END OF HashMapCounterTest
