package com.gengoai.conversion;

import com.gengoai.collection.Lists;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author David B. Bracewell
 */
public class LinkedListTest extends BaseCollectionConversionTest {

   public LinkedListTest() {
      super(LinkedList.class);
   }

   @Override
   protected Collection<?> create(Object... items) {
      return Lists.asLinkedList(Arrays.asList(items));
   }
}
