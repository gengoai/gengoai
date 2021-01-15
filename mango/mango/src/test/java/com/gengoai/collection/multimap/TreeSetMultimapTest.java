package com.gengoai.collection.multimap;

import java.util.TreeSet;

/**
 * @author David B. Bracewell
 */
public class TreeSetMultimapTest extends BaseMultimapTest {
   public TreeSetMultimapTest() {
      super(TreeSetMultimap::new, TreeSet::new);
   }
}