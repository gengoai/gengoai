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

package com.gengoai.apollo.math.linalg.nd3.dense;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import lombok.NonNull;

public class DenseInt64Factory extends NDArrayFactory<Long> {

   public DenseInt64Factory() {
      super(long.class);
   }

   @Override
   public NDArray<Long> array(@NonNull Shape shape, long[] a) {
      if (shape.length() != a.length) {
         throw new IllegalArgumentException("Length mismatch " + a.length + " != " + shape.length());
      } else if (shape.isEmpty()) {
         return empty();
      }
      return new DenseInt64NDArray(shape, a);
   }

   @Override
   public NDArray<Long> array(long[] a) {
      if (a.length == 0) {
         return empty();
      }
      return new DenseInt64NDArray(a);
   }

   @Override
   public NDArray<Long> array(long[][] a) {
      if (a.length == 0 || a[0].length == 0) {
         return empty();
      }
      return new DenseInt64NDArray(a);
   }

   @Override
   public NDArray<Long> array(long[][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0) {
         return empty();
      }
      return new DenseInt64NDArray(a);
   }

   @Override
   public NDArray<Long> array(long[][][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0 || a[0][0][0].length == 0) {
         return empty();
      }
      return new DenseInt64NDArray(a);
   }

   @Override
   protected NDArray<Long> fromArray(Object array, int depth) {
      switch (depth) {
         case 0:
            return scalar((Long) array);
         case 1:
            return array((long[]) array);
         case 2:
            return array((long[][]) array);
         case 3:
            return array((long[][][]) array);
         case 4:
            return array((long[][][][]) array);
      }
      return null;
   }

   @Override
   protected NDArray<Long> fromShapedArray(Shape shape, Object array, int depth) {
      switch (depth) {
         case 0:
            return scalar((Long) array);
         case 1:
            return array(shape, (long[]) array);
         case 2:
            return array((long[][]) array);
         case 3:
            return array((long[][][]) array);
         case 4:
            return array((long[][][][]) array);
      }
      return null;
   }

   @Override
   public NDArray<Long> zeros(@NonNull Shape shape) {
      return new DenseInt64NDArray(shape.copy());
   }
}
