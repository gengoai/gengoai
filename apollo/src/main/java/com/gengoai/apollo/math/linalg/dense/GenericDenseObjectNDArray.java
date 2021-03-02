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

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.ObjectNDArray;
import com.gengoai.apollo.math.linalg.ObjectNDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.lang.reflect.Array;

public class GenericDenseObjectNDArray<T> extends ObjectNDArray<T> {
   private static final long serialVersionUID = 1L;
   private final Class<T> dType;
   private Object[][] data;

   public GenericDenseObjectNDArray(Shape shape, @NonNull Class<T> dType) {
      super(shape);
      this.dType = dType;
      this.data = new Object[shape.sliceLength()][shape.matrixLength()];
   }

   @Override
   public ObjectNDArrayFactory<T> factory() {
      return new GenericDenseObjectNDArrayFactory<>(dType);
   }

   @Override
   public T get(int kernel, int channel, int row, int col) {
      return Cast.as(data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)]);
   }

   @Override
   public Class<?> getType() {
      return dType;
   }

   @Override
   public boolean isDense() {
      return true;
   }

   @Override
   public boolean isNumeric() {
      return Number.class.isAssignableFrom(dType);
   }

   @Override
   public ObjectNDArray<T> reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException();
      }
      GenericDenseObjectNDArray<T> temp = new GenericDenseObjectNDArray<>(newShape, dType);
      for (int i = 0; i < length(); i++) {
         temp.set(i, get(i));
      }
      this.data = temp.data;
      shape().reshape(newShape);
      return this;
   }

   @Override
   public ObjectNDArray<T> set(int kernel, int channel, int row, int col, Object value) {
      Validation.checkArgument(value == null || dType.isAssignableFrom(value.getClass()),
                               () -> "Cannot set value of type '" + value.getClass().getSimpleName()
                                     + "' for a '" + dType.getSimpleName() + "' NDArray");
      data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)] = Cast.as(value);
      return this;
   }

   @Override
   public ObjectNDArray<T> slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      GenericDenseObjectNDArray<T> v = new GenericDenseObjectNDArray<>(os, dType);
      for (int kernel = startKernel; kernel < endKernel; kernel++) {
         for (int channel = startChannel; channel < endChannel; channel++) {
            int ti = shape().calculateSliceIndex(kernel, channel);
            int oi = os.calculateSliceIndex(kernel - startKernel, channel - startChannel);
            v.data[oi] = data[ti];
         }
      }
      return v;
   }

   @Override
   public ObjectNDArray<T> slice(int index) {
      GenericDenseObjectNDArray<T> v = new GenericDenseObjectNDArray<T>(Shape.shape(shape().rows(),
                                                                                    shape().columns()),
                                                                        dType);
      v.data[0] = data[index];
      return v;
   }

   @Override
   public T[] toArray() {
      T[] out = Cast.as(Array.newInstance(getType(), (int) length()));
      for (long i = 0; i < length(); i++) {
         out[(int) i] = get(i);
      }
      return out;
   }
}
