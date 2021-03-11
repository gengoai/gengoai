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

package com.gengoai.apollo.math.linalg.sparse;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.NumericNDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import lombok.NonNull;

/**
 * <p>Factory for creating Sparse 64-bit flot NDArrays</p>
 *
 * @author David B. Bracewell
 */
public class SparseFloat64NDArrayFactory extends NumericNDArrayFactory {
   /**
    * The singleton instance of the Factory
    */
   public static final SparseFloat64NDArrayFactory INSTANCE = new SparseFloat64NDArrayFactory();
   private static final long serialVersionUID = 1L;

   protected SparseFloat64NDArrayFactory() {
      super(double.class);
   }

   @Override
   public NumericNDArray array(@NonNull Shape shape, double[] a) {
      if (shape.length() != a.length) {
         throw new IllegalArgumentException("Length mismatch " + a.length + " != " + shape.length());
      } else if (shape.isEmpty()) {
         return empty();
      }
      return new SparseFloat64NDArray(shape, a);
   }

   @Override
   public NumericNDArray scalar(@NonNull Number value) {
      return zeros(Shape.shape(1)).set(0, value.doubleValue());
   }

   @Override
   public NumericNDArray zeros(@NonNull Shape shape) {
      return new SparseFloat64NDArray(shape);
   }

}//END OF SparseFloat64NDArrayFactory
