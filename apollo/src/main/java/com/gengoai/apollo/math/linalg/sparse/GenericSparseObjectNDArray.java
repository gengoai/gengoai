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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.math.linalg.ObjectNDArray;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * <p>Sparse NDArray representing Generic Object values.</p>
 *
 * @author David B. Bracewell
 */
public class GenericSparseObjectNDArray<T> extends ObjectNDArray<T> {
   private static final long serialVersionUID = 1L;
   @JsonProperty("dType")
   private final Class<T> dType;
   private OpenIntObjectHashMap<T>[] data;

   @SuppressWarnings("unchecked")
   public GenericSparseObjectNDArray(@NonNull Shape shape, @NonNull Class<T> dType) {
      super(shape);
      this.dType = dType;
      this.data = new OpenIntObjectHashMap[shape.sliceLength()];
      for (int i = 0; i < shape.sliceLength(); i++) {
         this.data[i] = new OpenIntObjectHashMap<>();
      }
   }

   protected GenericSparseObjectNDArray(@NonNull Shape shape, @NonNull Class<T> dType, @NonNull T[] v) {
      this(shape, dType);
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }

   @JsonCreator
   protected GenericSparseObjectNDArray(@JsonProperty("data") T[] data,
                                        @JsonProperty("dType") Class<T> dType,
                                        @JsonProperty("shape") Shape shape,
                                        @JsonProperty("label") Object label,
                                        @JsonProperty("predicted") Object predicted,
                                        @JsonProperty("weight") double weight) {
      this(shape, dType, data);
      setLabel(label);
      setPredicted(predicted);
      setWeight(weight);
   }

   @Override
   public ObjectNDArray<T> compact() {
      for (OpenIntObjectHashMap<T> datum : data) {
         datum.trimToSize();
      }
      return this;
   }


   @Override
   public void forEachSparse(@NonNull EntryConsumer<T> consumer) {
      for (int i = 0; i < data.length; i++) {
         OpenIntObjectHashMap<T> map = data[i];
         int offset = i * shape().matrixLength();
         map.forEachPair((m, s) -> {
            consumer.apply(offset + m, s);
            return true;
         });
      }
   }

   @Override
   public T get(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)].get(shape().calculateMatrixIndex(row, col));
   }

   @Override
   public Class<?> getType() {
      return dType;
   }

   @Override
   public boolean isDense() {
      return false;
   }

   @Override
   public ObjectNDArray<T> reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException("Cannot change total length from " +
                                                  shape().length() +
                                                  " to " +
                                                  newShape.length());
      }
      GenericSparseObjectNDArray<T> temp = new GenericSparseObjectNDArray<>(newShape, dType);
      forEachSparse((index, value) -> {
         temp.set((int) index, value);
      });
      this.data = temp.data;
      shape().reshape(newShape);
      return this;
   }

   @Override
   public ObjectNDArray<T> set(int kernel, int channel, int row, int col, Object value) {
      int slice = shape().calculateSliceIndex(kernel, channel);
      int matrix = shape().calculateMatrixIndex(row, col);
      if (value == null) {
         data[slice].removeKey(matrix);
      } else {
         if (dType.isInstance(value)) {
            data[slice].put(matrix, Cast.as(value));
         } else {
            throw new IllegalArgumentException("Cannot use object of type '" + value.getClass().getSimpleName() +
                                                     "' as value for  for NDArray of type '" + dType.getSimpleName() +
                                                     "'");
         }
      }
      return this;
   }

   @Override
   public long size() {
      return Arrays.stream(data).mapToLong(OpenIntObjectHashMap::size).sum();
   }

   @Override
   public ObjectNDArray<T> slice(int index) {
      GenericSparseObjectNDArray<T> v = new GenericSparseObjectNDArray<T>(Shape.shape(shape().rows(),
                                                                                      shape().columns()),
                                                                          dType);
      if (data.length == 1) {
         v.data[0] = data[0];
      } else {
         v.data[0] = data[index];
      }
      return v;
   }

   @Override
   public ObjectNDArray<T> slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      GenericSparseObjectNDArray<T> v = new GenericSparseObjectNDArray<T>(os, dType);
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
   public long[] sparseIndices() {
      LongArrayList ial = new LongArrayList();
      forEachSparse(((index, value) -> ial.add(index)));
      ial.sort();
      return ial.toArray(new long[0]);
   }

   @Override
   @JsonProperty("data")
   public T[] toArray() {
      T[] out = Cast.as(Array.newInstance(dType, (int) length()));
      forEachSparse((index, value) -> out[(int) index] = value);
      return out;
   }

}//END OF GenericSparseObjectNDArray
