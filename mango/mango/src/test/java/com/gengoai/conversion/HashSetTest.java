package com.gengoai.conversion;

import com.gengoai.collection.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author David B. Bracewell
 */
public class HashSetTest extends BaseCollectionConversionTest {

   public HashSetTest() {
      super(HashSet.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return Sets.asHashSet(Arrays.asList(items));
   }
}
