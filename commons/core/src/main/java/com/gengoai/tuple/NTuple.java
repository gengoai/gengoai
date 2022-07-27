package com.gengoai.tuple;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Generic N-degree tuple.</p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class NTuple extends Tuple {
   private static final long serialVersionUID = 1L;
   @JsonProperty("array")
   private final Object[] array;

   /**
    * Instantiates a new N tuple.
    *
    * @param other the other
    */
   @JsonCreator
   public NTuple(@NonNull @JsonProperty Object[] other) {
      array = new Object[other.length];
      System.arraycopy(other, 0, array, 0, other.length);
   }

   /**
    * Creates an NTuple of the given items
    *
    * @param items the items
    * @return the NTuple
    */
   public static NTuple of(@NonNull Object... items) {
      return new NTuple(items);
   }

   /**
    * Creates an NTuple of the given items
    *
    * @param items the items
    * @return the NTuple
    */
   public static NTuple of(@NonNull List<?> items) {
      return new NTuple(items.toArray());
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

   @Override
   public Iterator<Object> iterator() {
      return Arrays.asList(array).iterator();
   }

   @Override
   public String toString() {
      return Strings.join(array(), ", ", "(", ")");
   }

}// END OF NTuple
