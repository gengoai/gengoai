/*
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

package com.gengoai.apollo.math.linalg.nd3;

import lombok.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

class IntervalRange implements IndexRange {
   protected final Coordinate lower;
   protected final Coordinate upper;
   protected final boolean bounded;
   protected final Shape shape;

   public IntervalRange(@NonNull Coordinate lower, @NonNull Coordinate upper, boolean bounded) {
      this.lower = lower;
      this.upper = upper;
      this.bounded = bounded;
      int[] index = new int[lower.point.length];
      boolean requiresNonZero = false;
      for (int i = 0; i < lower.point.length; i++) {
         index[i] = upper.point[i] - lower.point[i];
         if (requiresNonZero) {
            index[i] = Math.max(1, index[i]);
         }
         if (index[i] > 0) {
            requiresNonZero = true;
         }
      }
      this.shape = Shape.shape(index);
   }

   @Override
   public boolean contains(@NonNull Coordinate o) {
      return shape.contains(o);
   }

   @Override
   public Iterator<Index> iterator() {
      return new IteratorRange(new Index(lower), new Index(upper), bounded);
   }

   @Override
   public Index lower() {
      return new Index(lower);
   }

   @Override
   public Shape shape() {
      return shape.copy();
   }

   @Override
   public Index upper() {
      return new Index(upper);
   }

   protected static class IteratorRange implements Iterator<Index> {
      protected final Coordinate upper;
      protected final Index bounded;
      protected Index current;
      protected Index next;


      public IteratorRange(Index lower,
                           Index upper,
                           boolean bounded) {
         this.upper = upper;
         this.current = lower;
         this.bounded = bounded ? lower.copy() : null;
         if (current.compareTo(upper) < 0) {
            this.next = current;
         }
      }

      protected boolean computeNext() {
         if (next != null) {
            return true;
         }
         if (current == null) {
            return false;
         }
         if (increment()) {
            next = current;
            return true;
         }
         current = null;
         return false;
      }

      @Override
      public boolean hasNext() {
         return computeNext();
      }

      protected boolean increment() {
         for (int i = Coordinate.MAX_DIMENSIONS - 1; i >= 0; i--) {
            if (current.point[i] + 1 < upper.point[i]) {
               current.point[i]++;
               if (i + 1 < current.point.length && bounded == null) {
                  for (int j = i + 1; j < current.point.length; j++) {
                     current.point[j] = 0;
                  }
               } else {
                  for (int j = i + 1; j < current.point.length; j++) {
                     current.point[j] = bounded.get(j);
                  }
               }
               return true;
            }
         }
         return false;
      }

      @Override
      public Index next() {
         if (!computeNext()) {
            throw new NoSuchElementException();
         }
         next = null;
         return current.copy();
      }
   }

}
