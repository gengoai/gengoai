package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author David B. Bracewell
 */
public class PriorityBlockingQueueConversionTest extends BaseCollectionConversionTest {

   public PriorityBlockingQueueConversionTest() {
      super(PriorityBlockingQueue.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new PriorityBlockingQueue<>(Arrays.asList(items));
   }
}
