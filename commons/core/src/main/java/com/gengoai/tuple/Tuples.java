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

import lombok.NonNull;

import java.util.List;

/**
 * <p>Static constructor methods for Tuples using the pattern <code>&amp;(....);</code></p>
 *
 * @author David B. Bracewell
 */
public final class Tuples {

   private Tuples() {
      throw new IllegalAccessError();
   }


   /**
    * <p>Creates a tuple from a list of items. Empty or null lists return a 0-degree tuple.</p>
    *
    * @param list the list of objects.
    * @return the tuple
    */
   public static Tuple $(List<?> list) {
      if (list == null || list.size() == 0) {
         return Tuple0.INSTANCE;
      }
      switch (list.size()) {
         case 1:
            return Tuple1.of(list.get(0));
         case 2:
            return Tuple2.of(list.get(0), list.get(1));
         case 3:
            return Tuple3.of(list.get(0), list.get(1), list.get(2));
         case 4:
            return Tuple4.of(list.get(0), list.get(1), list.get(2), list.get(3));
         default:
            return NTuple.of(list);
      }
   }

   /**
    * Creates a triple from the given items
    *
    * @param <F>    the first type parameter
    * @param <S>    the second type parameter
    * @param <T>    the third type parameter
    * @param first  the first item
    * @param second the second item
    * @param third  the third item
    * @return the triple
    */
   public static <F, S, T> Tuple3<F, S, T> $(F first, S second, T third) {
      return Tuple3.of(first, second, third);
   }

   /**
    * Creates a tuple with degree four.
    *
    * @param <F>    the first type parameter
    * @param <S>    the second type parameter
    * @param <T>    the third type parameter
    * @param <D>    the fourth type parameter
    * @param first  the first item
    * @param second the second item
    * @param third  the third item
    * @param fourth the fourth item
    * @return the quadruple
    */
   public static <F, S, T, D> Tuple4<F, S, T, D> $(F first, S second, T third, D fourth) {
      return Tuple4.of(first, second, third, fourth);
   }

   /**
    * Creates a pair.
    *
    * @param <F>    the first type parameter
    * @param <S>    the second type parameter
    * @param first  the first item
    * @param second the second item
    * @return the pair
    */
   public static <F, S> Tuple2<F, S> $(F first, S second) {
      return Tuple2.of(first, second);
   }

   /**
    * Creates a tuple of degree zero.
    *
    * @return the tuple with degree zero.
    */
   public static Tuple0 $() {
      return Tuple0.INSTANCE;
   }

   /**
    * Creates a tuple of degree one.
    *
    * @param <F>   the first type parameter
    * @param first the first item
    * @return the tuple of degree one.
    */
   public static <F> Tuple1<F> $(F first) {
      return Tuple1.of(first);
   }

   /**
    * Creates a tuple from the given array of items
    *
    * @param items the items
    * @return the tuple
    */
   public static Tuple $(@NonNull Object... items) {
      switch (items.length) {
         case 0:
            return Tuple0.INSTANCE;
         case 1:
            return $(items[0]);
         case 2:
            return $(items[0], items[1]);
         case 3:
            return $(items[0], items[1], items[2]);
         case 4:
            return $(items[0], items[1], items[2], items[3]);
         default:
            return NTuple.of(items);
      }
   }

}//END OF Tuples
