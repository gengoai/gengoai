package com.gengoai.conversion;

import com.gengoai.collection.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author David B. Bracewell
 */
public class LinkedHashSetTest extends BaseCollectionConversionTest {

   public LinkedHashSetTest() {
      super(LinkedHashSet.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return Sets.asLinkedHashSet(Arrays.asList(items));
   }
}
