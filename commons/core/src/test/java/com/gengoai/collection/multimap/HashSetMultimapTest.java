package com.gengoai.collection.multimap;

import java.util.HashSet;

/**
 * @author David B. Bracewell
 */
public class HashSetMultimapTest extends BaseMultimapTest {

   public HashSetMultimapTest() {
      super(HashSetMultimap::new, HashSet::new);
   }
}