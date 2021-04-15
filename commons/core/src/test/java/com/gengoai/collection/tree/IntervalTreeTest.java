package com.gengoai.collection.tree;

import com.gengoai.collection.Iterables;
import com.gengoai.collection.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.TreeSet;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author David B. Bracewell
 */
public class IntervalTreeTest {

   private IntervalTree<Span> rb = new IntervalTree<>();
   private TreeSet<Span> set = new TreeSet<>();
   private IntervalTree<Span> tree = new IntervalTree<>();

   public static Span rnd() {
      int start;
      int end;
      do {
         start = (int) (Math.random() * 100);
         end = (int) (Math.random() * 500);
      } while (end <= start);
      return Span.of(start, end);
   }


   @Test
   public void ceiling() {
      for (Span span : set) {
         assertEquals(set.ceiling(span), Iterables.getFirst(rb.ceiling(span), null));
      }
   }

   @Test
   public void floor() {
      for (Span span : set) {
         assertEquals(set.floor(span), Iterables.getFirst(rb.floor(span), null));
      }
   }

   @Test
   public void higher() {
      for (Span span : set) {
         assertEquals(set.higher(span), Iterables.getFirst(rb.higher(span), null));
      }
   }

   @Test
   public void lower() {
      for (Span span : set) {
         assertEquals(set.lower(span), Iterables.getFirst(rb.lower(span), null));
      }
   }

   @Test
   public void overlapping() {
      IntervalTree<Span> simple = new IntervalTree<>();
      simple.addAll(Arrays.asList(Span.of(1, 2),
                                  Span.of(1, 3),
                                  Span.of(2, 4),
                                  Span.of(5, 10),
                                  Span.of(9, 10),
                                  Span.of(1, 10)));
      assertEquals(Sets.hashSetOf(Span.of(1, 3),
                                  Span.of(2, 4),
                                  Span.of(1, 10)),
                   Sets.asHashSet(simple.overlapping(Span.of(2, 4))));
      assertEquals(Sets.hashSetOf(Span.of(1, 3),
                                  Span.of(1, 2),
                                  Span.of(1, 10)),
                   Sets.asHashSet(simple.overlapping(Span.of(1, 2))));
      assertTrue(Iterables.isEmpty(simple.overlapping(Span.of(11, 22))));
   }

   @Before
   public void setup() {
      while (set.size() < 50) {
         Span s = rnd();
         if (!set.contains(s)) {
            set.add(s);
            tree.add(s);
            rb.add(s);
         }
      }
   }
}