package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.PriorityQueue;

/**
 * @author David B. Bracewell
 */
public class PriorityQueueConversionTest extends BaseCollectionConversionTest {

   public PriorityQueueConversionTest() {
      super(PriorityQueue.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new PriorityQueue<>(Arrays.asList(items));
   }
}
