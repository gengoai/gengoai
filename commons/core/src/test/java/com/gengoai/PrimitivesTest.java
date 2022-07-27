package com.gengoai;

import org.junit.Test;

import static com.gengoai.collection.Lists.arrayListOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class PrimitivesTest {

   @Test
   public void defaultValue() {
      assertEquals(0, Primitives.defaultValue(Integer.class), 0);
      assertEquals(0, Primitives.defaultValue(Long.class), 0);
      assertEquals(0, Primitives.defaultValue(Float.class), 0);
      assertEquals(0, Primitives.defaultValue(Double.class), 0);
      assertEquals(0, Primitives.defaultValue(Byte.class), 0);
      assertEquals(0, Primitives.defaultValue(Short.class), 0);
      assertEquals(0, Primitives.defaultValue(Character.class), 0);
      assertEquals(false, Primitives.defaultValue(Boolean.class));
      assertNull(Primitives.defaultValue(String.class));

      assertEquals(0, Primitives.defaultValue(int.class), 0);
      assertEquals(0, Primitives.defaultValue(long.class), 0);
      assertEquals(0, Primitives.defaultValue(float.class), 0);
      assertEquals(0, Primitives.defaultValue(double.class), 0);
      assertEquals(0, Primitives.defaultValue(byte.class), 0);
      assertEquals(0, Primitives.defaultValue(short.class), 0);
      assertEquals(0, Primitives.defaultValue(char.class), 0);
      assertEquals(false, Primitives.defaultValue(boolean.class));

   }

   @Test
   public void toByteArray() {
      assertArrayEquals(new byte[]{1, 3, 2},
                        Primitives.toByteArray(arrayListOf(1, 3, 2)));
   }

   @Test
   public void toCharArray() {
      assertArrayEquals(new char[]{'a', 'b', 'c'},
                        Primitives.toCharArray(arrayListOf('a', 'b', 'c')));

   }

   @Test
   public void toDoubleArray() {
      assertArrayEquals(new double[]{1d, 3d, 2d},
                        Primitives.toDoubleArray(arrayListOf(1, 3, 2)), 0d);
   }

   @Test
   public void toFloatArray() {
      assertArrayEquals(new float[]{1, 3, 2},
                        Primitives.toFloatArray(arrayListOf(1, 3, 2)), 0f);
   }

   @Test
   public void toIntArray() {
      assertArrayEquals(new int[]{1, 3, 2},
                        Primitives.toIntArray(arrayListOf(1, 3, 2)));
   }

   @Test
   public void toLongArray() {
      assertArrayEquals(new long[]{1, 3, 2},
                        Primitives.toLongArray(arrayListOf(1, 3, 2)));
   }

   @Test
   public void toShortArray() {
      assertArrayEquals(new short[]{1, 3, 2},
                        Primitives.toShortArray(arrayListOf(1, 3, 2)));
   }

   @Test
   public void unwrap() {
      assertEquals(int.class, Primitives.unwrap(Integer.class));
      assertEquals(double.class, Primitives.unwrap(Double.class));
      assertEquals(float.class, Primitives.unwrap(Float.class));
      assertEquals(float.class, Primitives.unwrap(float.class));
      assertEquals(String.class, Primitives.unwrap(String.class));
   }

   @Test
   public void wrap() {
      assertEquals(Integer.class, Primitives.wrap(int.class));
      assertEquals(Integer.class, Primitives.wrap(Integer.class));
      assertEquals(Double.class, Primitives.wrap(double.class));
      assertEquals(Float.class, Primitives.wrap(float.class));
      assertEquals(String.class, Primitives.unwrap(String.class));
   }
}