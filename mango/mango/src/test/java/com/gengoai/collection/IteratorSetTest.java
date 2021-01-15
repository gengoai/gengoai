package com.gengoai.collection;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.gengoai.collection.Lists.arrayListOf;
import static com.gengoai.collection.Sets.hashSetOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class IteratorSetTest {

   Set<String> set;

   @Before
   public void setUp() throws Exception {
      List<String> list = arrayListOf("A", "A", "B", "C");
      set = new IteratorSet<>(list::iterator);
   }

   @Test
   public void eq() {
      assertEquals(hashSetOf("A", "B", "C"), set);
   }

   @Test
   public void remove() {
      set.remove("A");
      assertEquals(hashSetOf("B", "C"), set);


      Iterator<String> itr = set.iterator();
      itr.next();
      itr.remove();

      assertEquals(1, set.size(), 0);

   }
}