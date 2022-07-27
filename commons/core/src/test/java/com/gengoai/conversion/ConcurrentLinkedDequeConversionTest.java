package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author David B. Bracewell
 */
public class ConcurrentLinkedDequeConversionTest extends BaseCollectionConversionTest {

   public ConcurrentLinkedDequeConversionTest() {
      super(ConcurrentLinkedDeque.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new ConcurrentLinkedDeque<>(Arrays.asList(items));
   }
}
