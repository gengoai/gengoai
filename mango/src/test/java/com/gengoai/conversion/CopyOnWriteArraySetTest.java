package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author David B. Bracewell
 */
public class CopyOnWriteArraySetTest extends BaseCollectionConversionTest {

   public CopyOnWriteArraySetTest() {
      super(CopyOnWriteArraySet.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new CopyOnWriteArraySet<>(Arrays.asList(items));
   }
}
