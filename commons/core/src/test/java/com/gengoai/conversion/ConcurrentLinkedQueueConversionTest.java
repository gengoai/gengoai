package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author David B. Bracewell
 */
public class ConcurrentLinkedQueueConversionTest extends BaseCollectionConversionTest {

   public ConcurrentLinkedQueueConversionTest() {
      super(ConcurrentLinkedQueue.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new ConcurrentLinkedQueue<>(Arrays.asList(items));
   }
}
