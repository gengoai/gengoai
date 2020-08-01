package com.gengoai.collection.multimap;

import java.util.LinkedList;

/**
 * @author David B. Bracewell
 */
public class LinkedListMultimapTest extends BaseMultimapTest {
   public LinkedListMultimapTest() {
      super(LinkedListMultimap::new, LinkedList::new);
   }

}