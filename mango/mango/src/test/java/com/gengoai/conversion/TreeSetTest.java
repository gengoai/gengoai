package com.gengoai.conversion;

import com.gengoai.collection.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author David B. Bracewell
 */
public class TreeSetTest extends BaseCollectionConversionTest {

   public TreeSetTest() {
      super(TreeSet.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return Sets.asTreeSet(Arrays.asList(items));
   }
}
