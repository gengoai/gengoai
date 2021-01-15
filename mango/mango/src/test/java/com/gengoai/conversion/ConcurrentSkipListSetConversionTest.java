package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author David B. Bracewell
 */
public class ConcurrentSkipListSetConversionTest extends BaseCollectionConversionTest {

   public ConcurrentSkipListSetConversionTest() {
      super(ConcurrentSkipListSet.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new ConcurrentSkipListSet<>(Arrays.asList(items));
   }
}
