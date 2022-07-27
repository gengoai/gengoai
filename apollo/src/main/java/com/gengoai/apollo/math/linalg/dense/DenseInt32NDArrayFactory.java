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

package com.gengoai.apollo.math.linalg.dense;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.NumericNDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import lombok.NonNull;

/**
 * <p>Factory for creating Dense 32-bit int NDArrays</p>
 *
 * @author David B. Bracewell
 */
public final class DenseInt32NDArrayFactory extends NumericNDArrayFactory {
   /**
    * The singleton instance of the Factory
    */
   public static final DenseInt32NDArrayFactory INSTANCE = new DenseInt32NDArrayFactory();
   private static final long serialVersionUID = 1L;

   private DenseInt32NDArrayFactory() {
      super(int.class);
   }

   @Override
   public NumericNDArray array(@NonNull Shape shape, int[] a) {
      if (shape.length() != a.length) {
         throw new IllegalArgumentException("Length mismatch " + a.length + " != " + shape.length());
      } else if (shape.isEmpty()) {
         return empty();
      }
      return new DenseInt32NDArray(shape, a);
   }

   @Override
   public NumericNDArray array(int[] a) {
      if (a.length == 0) {
         return empty();
      }
      return new DenseInt32NDArray(a);
   }

   @Override
   public NumericNDArray scalar(@NonNull Number value) {
      return new DenseInt32NDArray(new int[]{value.intValue()});
   }

   @Override
   public NumericNDArray zeros(@NonNull Shape shape) {
      return new DenseInt32NDArray(shape);
   }
}//END OF DenseInt32NDArrayFactory

