package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author David B. Bracewell
 */
public class LinkedBlockingQueueConversionTest extends BaseCollectionConversionTest {

   public LinkedBlockingQueueConversionTest() {
      super(LinkedBlockingQueue.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new LinkedBlockingQueue<>(Arrays.asList(items));
   }
}
