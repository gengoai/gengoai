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

import com.gengoai.apollo.math.linalg.ObjectNDArray;
import com.gengoai.apollo.math.linalg.ObjectNDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

/**
 * <p>Factory for creating Sparse Generic Object NDArrays</p>
 *
 * @author David B. Bracewell
 */
public class GenericSparseObjectNDArrayFactory<T> extends ObjectNDArrayFactory<T> {
   public static final SparseStringNDArrayFactory INSTANCE = new SparseStringNDArrayFactory();
   private static final long serialVersionUID = 1L;

   public GenericSparseObjectNDArrayFactory(@NonNull Class<T> dType) {
      super(dType);
   }

   @Override
   public ObjectNDArray<T> array(@NonNull Shape shape, T[] a) {
      if (shape.length() != a.length) {
         throw new IllegalArgumentException("Length mismatch " + a.length + " != " + shape.length());
      } else if (shape.isEmpty()) {
         return empty();
      }
      return new GenericSparseObjectNDArray<T>(shape, Cast.as(getType()), a);
   }


   @Override
   public ObjectNDArray<T> scalar(T value) {
      return zeros(Shape.shape(1)).set(0, value);
   }

   @Override
   public ObjectNDArray<T> zeros(@NonNull Shape shape) {
      return new GenericSparseObjectNDArray<T>(shape, Cast.as(getType()));
   }

}//END OF GenericSparseObjectNDArrayFactory
