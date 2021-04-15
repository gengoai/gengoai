package com.gengoai;

import com.gengoai.collection.Lists;
import com.gengoai.conversion.Cast;
import com.gengoai.function.Switch;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.gengoai.Validation.notNull;
import static com.gengoai.function.Switch.$switch;

/**
 * <p>Methods for working with primitive values including wrapping, unwrapping to object types and converting
 * collections.</p>
 *
 * @author David B. Bracewell
 */
public final class Primitives {

   private static final Switch<Object> defaultValues = $switch($ -> {
      $.instanceOf(Boolean.class, false);
      $.instanceOf(Byte.class, (byte) 0);
      $.instanceOf(Character.class, (char) 0);
      $.instanceOf(Double.class, 0d);
      $.instanceOf(Float.class, 0f);
      $.instanceOf(Integer.class, 0);
      $.instanceOf(Long.class, 0L);
      $.instanceOf(Short.class, (short) 0);
      $.defaultNull();
   });
   private static final Map<Class<?>, Class<?>> primitiveToWrap = new HashMap<>(20);
   private static final Map<Class<?>, Class<?>> wrapToPrimitive = new HashMap<>(20);

   static {
      add(int.class, Integer.class);
      add(short.class, Short.class);
      add(long.class, Long.class);
      add(char.class, Character.class);
      add(boolean.class, Boolean.class);
      add(void.class, Void.class);
      add(byte.class, Byte.class);
      add(float.class, Float.class);
      add(double.class, Double.class);
   }

   /**
    * Try to parse a string into a double returning null if a parse error occurs
    *
    * @param string the string to parse
    * @return the parsed Double value or null if not parsable
    */
   public static Double tryParseDouble(String string) {
      try {
         return Double.parseDouble(string);
      } catch (NumberFormatException t) {
         return null;
      }
   }

   /**
    * Try to parse a string into a float returning null if a parse error occurs
    *
    * @param string the string to parse
    * @return the parsed Float value or null if not parsable
    */
   public static Float tryParseFloat(String string) {
      try {
         return Float.parseFloat(string);
      } catch (NumberFormatException t) {
         return null;
      }
   }

   /**
    * Try to parse a string into a integer returning null if a parse error occurs
    *
    * @param string the string to parse
    * @return the parsed Integer value or null if not parsable
    */
   public static Integer tryParseInteger(String string) {
      try {
         return Integer.parseInt(string);
      } catch (NumberFormatException t) {
         return null;
      }
   }

   /**
    * Try to parse a string into a long returning null if a parse error occurs
    *
    * @param string the string to parse
    * @return the parsed long value or null if not parsable
    */
   public static Long tryParseLong(String string) {
      try {
         return Long.parseLong(string);
      } catch (NumberFormatException t) {
         return null;
      }
   }

   private static void add(Class<?> primitive, Class<?> wrap) {
      primitiveToWrap.put(primitive, wrap);
      wrapToPrimitive.put(wrap, primitive);
   }

   /**
    * Gets the default value of the given class (primitive and boxed versions all other classes will result in null)
    *
    * @param <T>   the type parameter
    * @param clazz the clazz
    * @return the default value (null for non-primitives and their boxed types)
    */
   public static <T> T defaultValue(@NonNull Class<T> clazz) {
      return Cast.as(defaultValues.apply(wrap(clazz)));
   }

   /**
    * Converts and iterable of numbers to an array of byte
    *
    * @param numbers the numbers to convert
    * @return the byte array
    */
   public static byte[] toByteArray(@NonNull Iterable<? extends Number> numbers) {
      Collection<? extends Number> collection;
      if(numbers instanceof Collection) {
         collection = Cast.as(numbers);
      } else {
         collection = Lists.asArrayList(numbers);
      }
      byte[] bytes = new byte[collection.size()];
      int i = 0;
      for(Number number : collection) {
         bytes[i] = number.byteValue();
         i++;
      }
      return bytes;
   }

   /**
    * Converts and iterable of Character to an array of char
    *
    * @param characters the characters to convert
    * @return the char array
    */
   public static char[] toCharArray(@NonNull Iterable<Character> characters) {
      Collection<Character> collection;
      if(characters instanceof Collection) {
         collection = Cast.as(characters);
      } else {
         collection = Lists.asArrayList(characters);
      }
      char[] chars = new char[collection.size()];
      int i = 0;
      for(Character c : collection) {
         chars[i] = c;
         i++;
      }
      return chars;
   }

   /**
    * Converts and iterable of numbers to an array of double
    *
    * @param numbers the numbers to convert
    * @return the double array
    */
   public static double[] toDoubleArray(@NonNull Iterable<? extends Number> numbers) {
      Collection<? extends Number> collection;
      if(numbers instanceof Collection) {
         collection = Cast.as(numbers);
      } else {
         collection = Lists.asArrayList(numbers);
      }
      double[] toReturn = new double[collection.size()];
      int i = 0;
      for(Number number : collection) {
         toReturn[i] = number.doubleValue();
         i++;
      }
      return toReturn;
   }

   /**
    * Converts and iterable of numbers to an array of float
    *
    * @param numbers the numbers to convert
    * @return the float array
    */
   public static float[] toFloatArray(@NonNull Iterable<? extends Number> numbers) {
      Collection<? extends Number> collection;
      if(numbers instanceof Collection) {
         collection = Cast.as(numbers);
      } else {
         collection = Lists.asArrayList(numbers);
      }
      float[] toReturn = new float[collection.size()];
      int i = 0;
      for(Number number : collection) {
         toReturn[i] = number.floatValue();
         i++;
      }
      return toReturn;
   }

   /**
    * Converts and iterable of numbers to an array of int
    *
    * @param numbers the numbers to convert
    * @return the int array
    */
   public static int[] toIntArray(@NonNull Iterable<? extends Number> numbers) {
      Collection<? extends Number> collection;
      if(numbers instanceof Collection) {
         collection = Cast.as(numbers);
      } else {
         collection = Lists.asArrayList(numbers);
      }
      int[] toReturn = new int[collection.size()];
      int i = 0;
      for(Number number : collection) {
         toReturn[i] = number.intValue();
         i++;
      }
      return toReturn;
   }

   /**
    * Converts and iterable of numbers to an array of long
    *
    * @param numbers the numbers to convert
    * @return the long array
    */
   public static long[] toLongArray(@NonNull Iterable<? extends Number> numbers) {
      Collection<? extends Number> collection;
      if(numbers instanceof Collection) {
         collection = Cast.as(numbers);
      } else {
         collection = Lists.asArrayList(numbers);
      }
      long[] toReturn = new long[collection.size()];
      int i = 0;
      for(Number number : collection) {
         toReturn[i] = number.longValue();
         i++;
      }
      return toReturn;
   }

   /**
    * Converts and iterable of numbers to an array of short
    *
    * @param numbers the numbers to convert
    * @return the short array
    */
   public static short[] toShortArray(@NonNull Iterable<? extends Number> numbers) {
      Collection<? extends Number> collection;
      if(numbers instanceof Collection) {
         collection = Cast.as(numbers);
      } else {
         collection = Lists.asArrayList(numbers);
      }
      short[] toReturn = new short[collection.size()];
      int i = 0;
      for(Number number : collection) {
         toReturn[i] = number.shortValue();
         i++;
      }
      return toReturn;
   }

   /**
    * Gets the primitive type class corresponding to an boxed type.
    *
    * @param <T>  the type parameter
    * @param type the boxed type
    * @return the primitive class
    */
   public static <T> Class<T> unwrap(Class<T> type) {
      notNull(type);
      return Cast.as(wrapToPrimitive.getOrDefault(type, type));
   }

   /**
    * Gets the object type class corresponding to a primitive class.
    *
    * @param <T>  the type parameter
    * @param type the primitive type
    * @return the wrapped type class
    */
   public static <T> Class<T> wrap(@NonNull Class<T> type) {
      return Cast.as(primitiveToWrap.getOrDefault(type, type));
   }

   private Primitives() {
      throw new IllegalAccessError();
   }

}//END OF Primitives
