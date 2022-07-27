package com.gengoai.collection;

import org.junit.Assert;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.gengoai.collection.Lists.arrayListOf;
import static com.gengoai.collection.Lists.asArrayList;
import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class IterablesTest {

   @Test
   public void arrayAsIterable() throws Exception {
      Assert.assertEquals(arrayListOf(1, 2, 3),
                          Lists.asArrayList(Iterables.asIterable(new int[]{1, 2, 3})));
      Assert.assertEquals(arrayListOf(1, 2, 3),
                          Lists.asArrayList(Iterables.asIterable(new Integer[]{1, 2, 3})));
      Assert.assertEquals(arrayListOf(1d, 2d, 3d),
                          Lists.asArrayList(Iterables.asIterable(new double[]{1.0, 2.0, 3.0})));
   }

   @Test
   public void concat() {
      Iterable<String> i1 = arrayListOf("A", "B");
      Iterable<String> i2 = arrayListOf("C", "D");
      Iterable<String> concat = Iterables.concat(i1, i2);
      assertEquals(arrayListOf("A", "B", "C", "D"), asArrayList(concat));
   }

   @Test
   public void filter() {
      Iterable<Integer> i1 = arrayListOf(1, 2, 3, 4, 5, 6);
      Iterable<Integer> filtered = Iterables.filter(i1, n -> n % 2 == 0);
      assertEquals(3, Iterables.size(filtered));
   }

   @Test
   public void getFirst() throws Exception {
      assertTrue(Iterables.getFirst(Arrays.asList("A", "B", "C")).isPresent());
      assertFalse(Iterables.getFirst(Collections.emptySet()).isPresent());
      assertEquals("A", Iterables.getFirst(Arrays.asList("A", "B", "C")).orElse(null));
   }

   @Test
   public void getLast() throws Exception {
      assertTrue(Iterables.getLast(Arrays.asList("A", "B", "C")).isPresent());
      assertFalse(Iterables.getLast(Collections.emptySet()).isPresent());
      assertEquals("C", Iterables.getLast(Arrays.asList("A", "B", "C")).orElse(null));
   }

   @Test
   public void iteratorToIterable() throws Exception {
      Iterable<CharSequence> ibl1 = Iterables.asIterable(arrayListOf("A", "B", "C").iterator());
      assertEquals(3, Iterables.size(ibl1));

      Iterable<CharSequence> ibl3 = Iterables.asIterable(Collections.emptyIterator());
      assertEquals(0, Iterables.size(ibl3));
   }

   @Test
   public void sort() throws Exception {
      Assert.assertEquals(arrayListOf("a", "b", "c"), Iterables.sort(Sets.hashSetOf("c", "b", "a")));
      Assert.assertEquals(arrayListOf(3, 2, 1),
                          Iterables.sort(arrayListOf(1, 2, 3), Sorting.natural().reversed()));
   }

   @Test
   public void transform() {
      Iterable<Integer> i1 = arrayListOf(1, 2, 3, 4, 5, 6);
      Iterable<Integer> transform = Iterables.transform(i1, n -> n + 1);
      assertEquals(7, Iterables.getLast(transform, -1), 0);
   }

   @Test
   public void zip() throws Exception {
      Assert.assertEquals(arrayListOf(new AbstractMap.SimpleEntry<>("A", 1),
                                      new AbstractMap.SimpleEntry<>("B", 2),
                                      new AbstractMap.SimpleEntry<>("C", 3)
                                     ),
                          Lists.asArrayList(Iterables.zip(Arrays.asList("A", "B", "C"), Arrays.asList(1, 2, 3, 4)))
                         );

      assertEquals(0L, Iterables.size(Iterables.zip(Collections.emptySet(), Arrays.asList(1, 2, 3, 4))));
   }

   @Test
   public void zipWithIndex() {
      Iterable<String> i1 = arrayListOf("A", "B");
      Iterable<Map.Entry<String, Integer>> withIndex = Iterables.zipWithIndex(i1);
      assertEquals($("A", 0), Iterables.getFirst(withIndex).orElseThrow(IndexOutOfBoundsException::new));
      assertEquals($("B", 1), Iterables.getLast(withIndex).orElseThrow(IndexOutOfBoundsException::new));
   }


}