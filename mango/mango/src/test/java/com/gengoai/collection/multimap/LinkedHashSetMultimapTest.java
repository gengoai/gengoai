package com.gengoai.collection.multimap;

import java.util.LinkedHashSet;

/**
 * @author David B. Bracewell
 */
public class LinkedHashSetMultimapTest extends BaseMultimapTest {

   public LinkedHashSetMultimapTest() {
      super(LinkedHashSetMultimap::new, LinkedHashSet::new);
   }
}