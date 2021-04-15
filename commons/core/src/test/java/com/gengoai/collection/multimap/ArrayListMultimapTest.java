package com.gengoai.collection.multimap;

import java.util.ArrayList;

/**
 * @author David B. Bracewell
 */
public class ArrayListMultimapTest extends BaseMultimapTest {

   public ArrayListMultimapTest() {
      super(ArrayListMultimap::new, ArrayList::new);
   }
}