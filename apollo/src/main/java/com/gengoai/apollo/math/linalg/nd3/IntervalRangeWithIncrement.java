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

class IntervalRangeWithIncrement extends IntervalRange {
   private final Coordinate increment;

   public IntervalRangeWithIncrement(@NonNull Coordinate lower, @NonNull Coordinate upper, boolean bounded, @NonNull Coordinate increment) {
      super(lower, upper, bounded);
      this.increment = increment;
   }

   @Override
   public Iterator<Index> iterator() {
      return new IteratorRangeWithIncrement(new Index(lower), new Index(upper), bounded, increment);
   }

   private static class IteratorRangeWithIncrement extends IntervalRange.IteratorRange {
      private final Coordinate increment;

//      public IteratorRangeWithIncrement(IndexRange range, boolean bounded, Coordinate increment) {
//         super(range,bounded);
//         this.increment = increment;
//      }
      public IteratorRangeWithIncrement(Index lower, Index upper, boolean bounded, Coordinate increment) {
         super(lower, upper, bounded);
         this.increment = increment;
      }

      @Override
      protected boolean increment() {
         for (int i = upper.point.length - 1; i >= 0; i--) {
            if (increment.get(i) != 0 && current.point[i] + increment.get(i) < upper.point[i]) {
               current.point[i] += increment.get(i);
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
   }

}
