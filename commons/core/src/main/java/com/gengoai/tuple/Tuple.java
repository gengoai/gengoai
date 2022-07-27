/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.tuple;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.Copyable;
import com.gengoai.Validation;
import com.gengoai.collection.Arrays2;
import com.gengoai.collection.Sorting;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A tuple is a finite sequence of items. Mango provides specific implementations for degree 0-4 tuples, which all
 * each element's type to be defined via generics. For tuples with degree 5 or more, a generic NTuple is provided.</p>
 *
 * @author David B. Bracewell
 */
@JsonDeserialize(as = NTuple.class)
public abstract class Tuple implements Iterable<Object>, Comparable<Tuple>, Copyable<Tuple>, Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Appends an item the beginning of the tuple resulting in a new tuple of degree + 1
    *
    * @param <T>    the type parameter
    * @param object the object being appended
    * @return A new tuple of degree + 1 containing the object at the beginning
    */
   public <T> Tuple appendLeft(T object) {
      if (degree() == 0) {
         return Tuple1.of(object);
      }
      return NTuple.of(Arrays2.concat(new Object[]{object}, array()));
   }

   /**
    * Appends an item the end of the tuple resulting in a new tuple of degree + 1
    *
    * @param <T>    the type parameter
    * @param object the object being appended
    * @return A new tuple of degree + 1 containing the object at the end
    */
   public <T> Tuple appendRight(T object) {
      if (degree() == 0) {
         return Tuple1.of(object);
      }
      return NTuple.of(Arrays2.concat(array(), new Object[]{object}));
   }

   /**
    * The tuple as an array of objects
    *
    * @return an array representing the items in the tuple
    */
   public abstract Object[] array();

   @Override
   public final int compareTo(Tuple o) {
      if (degree() < o.degree()) {
         return -1;
      } else if (degree() > o.degree()) {
         return 1;
      }
      Object[] a1 = array();
      Object[] a2 = o.array();
      for (int i = 0; i < a1.length; i++) {
         int cmp = Sorting.compare(a1[i], a2[i]);
         if (cmp != 0) {
            return cmp;
         }
      }
      return 0;
   }

   /**
    * The number of items in the tuple
    *
    * @return the number of items in the tuple
    */
   public abstract int degree();

   @Override
   public final boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj instanceof Tuple) {
         Tuple tuple = Cast.as(obj);
         return degree() == tuple.degree() && Arrays.equals(array(), tuple.array());
      } else if (obj instanceof Map.Entry && degree() == 2) {
         Map.Entry<?, ?> e = Cast.as(obj);
         return Objects.equals(e.getKey(), get(0)) && Objects.equals(e.getValue(), get(1));
      }
      return false;
   }

   /**
    * Gets the ith item of the tuple.
    *
    * @param <T> the type parameter
    * @param i   the index of the item
    * @return the item at the ith index
    */
   public <T> T get(int i) {
      return Cast.as(array()[i]);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(array());
   }

   @Override
   public Iterator<Object> iterator() {
      return Arrays.asList(array()).iterator();
   }

   /**
    * Maps the tuple to another a data type.
    *
    * @param <R>      the type being mapped to
    * @param function the mapping function
    * @return the result of the mapping function
    */
   public <R> R map(@NonNull Function<Tuple, R> function) {
      return function.apply(this);
   }

   /**
    * Maps the values of the tuple to another data type
    *
    * @param function the mapping function
    * @return A new tuple of same degree whose values are the result of the mapping function applied to the this tuple's
    * elements.
    */
   public Tuple mapValues(@NonNull Function<Object, ?> function) {
      return NTuple.of(Arrays.stream(array()).map(function).collect(Collectors.toList()));
   }

   /**
    * Shifts the first element of the tuple resulting in a tuple of degree - 1.
    *
    * @return A new tuple without the shifted element;
    */
   public Tuple shiftLeft() {
      if (degree() < 2) {
         return Tuple0.INSTANCE;
      }
      return NTuple.of(Arrays.copyOfRange(array(), 1, degree()));
   }

   /**
    * Shifts the last element of the tuple resulting in a tuple of degree - 1.
    *
    * @return A new tuple without the shifted element;
    */
   public Tuple shiftRight() {
      if (degree() < 2) {
         return Tuple0.INSTANCE;
      }
      return NTuple.of(Arrays.copyOfRange(array(), 0, degree() - 1));
   }

   /**
    * Takes a slice of the tuple from an inclusive start to an exclusive end index.
    *
    * @param start Where to start the slice from (inclusive)
    * @param end   Where to end the slice at (exclusive)
    * @return A new tuple of degree (end - start) with the elements of this tuple from start to end
    */
   public Tuple slice(int start, int end) {
      Validation.checkArgument(start >= 0, "Start index must be >= 0");
      Validation.checkArgument(start < end, "Start index must be < end index");
      if (start >= degree()) {
         return Tuple0.INSTANCE;
      }
      return new NTuple(Arrays.copyOfRange(array(), start, Math.min(end, degree())));
   }

   /**
    * Gets a stream over the objects in the tuple
    *
    * @return A Stream of the objects in the tuple
    */
   public Stream<Object> stream() {
      return Arrays.stream(array());
   }

   @Override
   public String toString() {
      return Strings.join(array(), ", ", "(", ")");
   }

}//END OF Tuple
