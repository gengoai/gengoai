package com.gengoai.collection;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author David B. Bracewell
 */
public class Arrays2Test {

   @Test
   public void arrayOf() {
      assertArrayEquals(new Object[]{"A", 1, 2.0},
                        Arrays2.<Object>arrayOf("A", 1, 2.0));
   }

   @Test
   public void arrayOfBoolean() {
      assertArrayEquals(new boolean[]{true, true, false},
                        Arrays2.arrayOfBoolean(true, true, false));
   }

   @Test
   public void arrayOfByte() {
      byte[] b = {1, 2, 3};
      assertArrayEquals(b, Arrays2.arrayOfByte(1, 2, 3));
      assertArrayEquals(b, Arrays2.arrayOfByte(b));
   }

   @Test
   public void arrayOfChar() {
      assertArrayEquals(new char[]{'a', 'b', 'c'},
                        Arrays2.arrayOfChar('a', 'b', 'c'));
   }

   @Test
   public void arrayOfDouble() {
      assertArrayEquals(new double[]{1.5, 2.5, 3.5},
                        Arrays2.arrayOfDouble(1.5, 2.5, 3.5), 0);
   }

   @Test
   public void arrayOfFloat() {
      assertArrayEquals(new float[]{1.5f, 2.5f, 3.5f},
                        Arrays2.arrayOfFloat(1.5f, 2.5f, 3.5f), 0);
   }

   @Test
   public void arrayOfInt() {
      assertArrayEquals(new int[]{1, 2, 3},
                        Arrays2.arrayOfInt(1, 2, 3));
   }

   @Test
   public void arrayOfShort() {
      short[] s = {1, 2, 3};
      assertArrayEquals(s, Arrays2.arrayOfShort(1, 2, 3));
      assertArrayEquals(s, Arrays2.arrayOfShort(s));
   }

   @Test
   public void arrayOfLong() {
      assertArrayEquals(new long[]{1L, 2L, 3L},
                        Arrays2.arrayOfLong(1, 2, 3));
   }
}