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

package com.gengoai.collection;

import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableComparator;

/**
 * Methods for comparing and sorting.
 *
 * @author David B. Bracewell
 */
public final class Sorting {

   private Sorting() {
      throw new IllegalAccessError();
   }

   /**
    * Natural serializable comparator.
    *
    * @param <E> the type parameter
    * @return the serializable comparator
    */
   public static <E> SerializableComparator<E> natural() {
      return Sorting::compare;
   }

   /**
    * Generic method for comparing to objects that is null safe. Assumes that the objects are <code>Comparable</code>.
    *
    * @param o1 the first object
    * @param o2 the second object
    * @return the comparison result
    * @throws IllegalArgumentException if the objects are non-null and not comparable
    */
   @SuppressWarnings("unchecked")
   public static int compare(Object o1, Object o2) {
      if (o1 == null && o2 == null) {
         return 0;
      } else if (o1 == null) {
         return 1;
      } else if (o2 == null) {
         return -1;
      }
      if (o1 instanceof Comparable && o2 instanceof Comparable) {
         return Cast.<Comparable>as(o1).compareTo(o2);
      }
      throw new IllegalArgumentException("objects must implement the Comparable interface");
   }

   /**
    * Compares two objects based on their hashcode.
    *
    * @param <T> the type of object being compared
    * @return A simplistic comparator that compares hash code values
    */
   public static <T> SerializableComparator<T> hashCodeComparator() {
      return (o1, o2) -> {
         if (o1 == o2) {
            return 0;
         } else if (o1 == null) {
            return 1;
         } else if (o2 == null) {
            return -1;
         }
         return Integer.compare(o1.hashCode(), o2.hashCode());
      };
   }


}// END OF Sorting
