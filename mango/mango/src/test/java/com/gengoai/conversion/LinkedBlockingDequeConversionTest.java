package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author David B. Bracewell
 */
public class LinkedBlockingDequeConversionTest extends BaseCollectionConversionTest {

   public LinkedBlockingDequeConversionTest() {
      super(LinkedBlockingDeque.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new LinkedBlockingDeque<>(Arrays.asList(items));
   }
}
