package com.gengoai.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author David B. Bracewell
 */
public class CopyOnWriteArrayListTest extends BaseCollectionConversionTest {

   public CopyOnWriteArrayListTest() {
      super(CopyOnWriteArrayList.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return new CopyOnWriteArrayList<>(Arrays.asList(items));
   }
}
