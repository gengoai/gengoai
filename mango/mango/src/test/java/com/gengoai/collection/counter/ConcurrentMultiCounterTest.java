package com.gengoai.collection.counter;

import static com.gengoai.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public class ConcurrentMultiCounterTest extends BaseMultiCounterTest {

   @Override
   public MultiCounter<String, String> getEmptyCounter() {
      return MultiCounters.newConcurrentMultiCounter();
   }

   @Override
   public MultiCounter<String, String> getEntryCounter() {
      return MultiCounters.newConcurrentMultiCounter($("A", "B"),
                                                     $("A", "C"),
                                                     $("A", "D"),
                                                     $("B", "E"),
                                                     $("B", "G"),
                                                     $("B", "H")
                                                    );
   }
}//END OF ConcurrentMultiCounterTest
