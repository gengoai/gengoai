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
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public final class DenseFloat32Factory extends NDArrayFactory<Float> {
   private static final long serialVersionUID = 1L;

   public DenseFloat32Factory() {
      super(float.class);
   }

   public NDArray<Float> array(float[] a) {
      if (a.length == 0) {
         return empty();
      }
      return new DenseFloat32NDArray(a);
   }

   public NDArray<Float> array(@NonNull FloatMatrix floatMatrix){
      return new DenseFloat32NDArray(floatMatrix);
   }

   public NDArray<Float> array(@NonNull DoubleMatrix doubleMatrix){
      return new DenseFloat32NDArray(MatrixFunctions.doubleToFloat(doubleMatrix));
   }

   public NDArray<Float> array(@NonNull Shape shape, float[] a) {
      if (shape.length() != a.length) {
         throw new IllegalArgumentException("Length mismatch " + a.length + " != " + shape.length());
      } else if (shape.isEmpty()) {
         return empty();
      }
      return new DenseFloat32NDArray(shape, a);
   }

   public NDArray<Float> array(float[][] a) {
      if (a.length == 0 || a[0].length == 0) {
         return empty();
      }
      return new DenseFloat32NDArray(a);
   }

   public NDArray<Float> array(float[][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0) {
         return empty();
      }
      return new DenseFloat32NDArray(a);
   }

   public NDArray<Float> array(float[][][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0 || a[0][0][0].length == 0) {
         return empty();
      }
      return new DenseFloat32NDArray(a);
   }

   @Override
   public NDArray<Float> empty() {
      return new DenseFloat32NDArray(FloatMatrix.EMPTY);
   }

   @Override
   protected NDArray<Float> fromArray(Object array, int depth) {
      switch (depth) {
         case 0:
            return scalar((Float) array);
         case 1:
            return array((float[]) array);
         case 2:
            return array((float[][]) array);
         case 3:
            return array((float[][][]) array);
         case 4:
            return array((float[][][][]) array);
      }
      return null;
   }

   @Override
   protected NDArray<Float> fromShapedArray(Shape shape, Object array, int depth) {
      switch (depth) {
         case 0:
            return scalar((Float) array);
         case 1:
            return array(shape, (float[]) array);
         case 2:
            return array((float[][]) array);
         case 3:
            return array((float[][][]) array);
         case 4:
            return array((float[][][][]) array);
      }
      return null;
   }

   @Override
   public NDArray<Float> scalar(@NonNull Float value) {
      return new DenseFloat32NDArray(Shape.shape(1), value);
   }


   @Override
   public NDArray<Float> zeros(@NonNull Shape shape) {
      return new DenseFloat32NDArray(shape);
   }

}


