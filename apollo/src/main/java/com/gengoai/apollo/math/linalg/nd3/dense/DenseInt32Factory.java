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

public final class DenseInt32Factory extends NDArrayFactory<Integer> {
   private static final long serialVersionUID = 1L;


   public DenseInt32Factory() {
      super(int.class);
   }

   @Override
   public NDArray<Integer> array(@NonNull Shape shape, int[] a) {
      if (shape.length() != a.length) {
         throw new IllegalArgumentException("Length mismatch " + a.length + " != " + shape.length());
      } else if (shape.isEmpty()) {
         return empty();
      }
      return new DenseInt32NDArray(shape, a);
   }

   @Override
   public NDArray<Integer> array(int[] a) {
      if (a.length == 0) {
         return empty();
      }
      return new DenseInt32NDArray(a);
   }

   @Override
   public NDArray<Integer> array(int[][] a) {
      if (a.length == 0 || a[0].length == 0) {
         return empty();
      }
      return new DenseInt32NDArray(a);
   }

   @Override
   public NDArray<Integer> array(int[][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0) {
         return empty();
      }
      return new DenseInt32NDArray(a);
   }

   @Override
   public NDArray<Integer> array(int[][][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0 || a[0][0][0].length == 0) {
         return empty();
      }
      return new DenseInt32NDArray(a);
   }

   @Override
   protected NDArray<Integer> fromArray(Object array, int depth) {
      switch (depth) {
         case 0:
            return scalar((Integer) array);
         case 1:
            return array((int[]) array);
         case 2:
            return array((int[][]) array);
         case 3:
            return array((int[][][]) array);
         case 4:
            return array((int[][][][]) array);
      }
      return null;
   }

   @Override
   protected NDArray<Integer> fromShapedArray(Shape shape, Object array, int depth) {
      switch (depth) {
         case 0:
            return scalar((Integer) array);
         case 1:
            return array(shape, (int[]) array);
         case 2:
            return array((int[][]) array);
         case 3:
            return array((int[][][]) array);
         case 4:
            return array((int[][][][]) array);
      }
      return null;
   }


   @Override
   public NDArray<Integer> zeros(@NonNull Shape shape) {
      return new DenseInt32NDArray(shape);
   }
}

