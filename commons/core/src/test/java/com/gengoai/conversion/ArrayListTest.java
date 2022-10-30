package com.gengoai.conversion;

import com.gengoai.collection.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author David B. Bracewell
 */
public class ArrayListTest extends BaseCollectionConversionTest {

   public ArrayListTest() {
      super(ArrayList.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return Lists.asArrayList(Arrays.asList(items));
   }
}
