package com.gengoai.tuple;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Generic Tuple of any degree.
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class NTuple extends Tuple {
   private static final long serialVersionUID = 1L;
   private final Object[] array;

   /**
    * Of n tuple.
    *
    * @param <T>   the type parameter
    * @param items the items
    * @return the n tuple
    */
   @SafeVarargs
   public static <T> NTuple of(T... items) {
      return new NTuple(items);
   }

   /**
    * Of n tuple.
    *
    * @param <T>   the type parameter
    * @param items the items
    * @return the n tuple
    */
   public static <T> NTuple of(List<T> items) {
      return new NTuple(items.toArray());
   }

   /**
    * Instantiates a new N tuple.
    *
    * @param other the other
    */
   @JsonCreator
   public NTuple(@JsonProperty Object[] other) {
      array = new Object[other.length];
      System.arraycopy(other, 0, array, 0, other.length);
   }

   @Override
   @JsonValue
   public Object[] array() {
      Object[] copy = new Object[array.length];
      System.arraycopy(array, 0, copy, 0, array.length);
      return copy;
   }

   @Override
   public NTuple copy() {
      return new NTuple(array);
   }

   @Override
   public int degree() {
      return array.length;
   }

   @Override
   public <T> T get(int i) {
      return Cast.as(array[i]);
   }

   public Object[] getArray() {
      return array();
   }

   @Override
   public Iterator<Object> iterator() {
      return Arrays.asList(array).iterator();
   }

   @Override
   public String toString() {
      return Strings.join(array(), ", ", "(", ")");
   }

}// END OF NTuple
