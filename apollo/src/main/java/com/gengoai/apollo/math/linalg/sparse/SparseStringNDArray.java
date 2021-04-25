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
import com.gengoai.apollo.math.linalg.*;
import lombok.NonNull;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import java.util.Arrays;

/**
 * <p>Sparse NDArray representing String values.</p>
 *
 * @author David B. Bracewell
 */
public class SparseStringNDArray extends StringNDArray {
   private static final long serialVersionUID = 1L;
   private OpenIntObjectHashMap<String>[] data;

   @SuppressWarnings("unchecked")
   protected SparseStringNDArray(Shape shape) {
      super(shape);
      this.data = new OpenIntObjectHashMap[shape.sliceLength()];
      for (int i = 0; i < shape.sliceLength(); i++) {
         this.data[i] = new OpenIntObjectHashMap<>();
      }
   }

   protected SparseStringNDArray(@NonNull Shape shape, @NonNull String[] v) {
      this(shape);
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }

   @JsonCreator
   protected SparseStringNDArray(@JsonProperty("data") String[] data,
                                 @JsonProperty("shape") Shape shape,
                                 @JsonProperty("label") Object label,
                                 @JsonProperty("predicted") Object predicted,
                                 @JsonProperty("weight") double weight) {
      this(shape, data);
      setLabel(label);
      setPredicted(predicted);
      setWeight(weight);
   }

   @Override
   public ObjectNDArray<String> compact() {
      for (OpenIntObjectHashMap<String> datum : data) {
         datum.trimToSize();
      }
      return this;
   }


   @Override
   public ObjectNDArrayFactory<String> factory() {
      return nd.SSTRING;
   }

   @Override
   public void forEachSparse(@NonNull EntryConsumer<String> consumer) {
      for (int i = 0; i < data.length; i++) {
         OpenIntObjectHashMap<String> map = data[i];
         int offset = i * shape().matrixLength();
         map.forEachPair((m, s) -> {
            consumer.apply(offset + m, s);
            return true;
         });
      }
   }

   @Override
   public String get(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)].get(shape().calculateMatrixIndex(row, col));
   }

   @Override
   public Class<?> getType() {
      return String.class;
   }

   @Override
   public boolean isDense() {
      return false;
   }

   @Override
   public ObjectNDArray<String> reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException("Cannot change total length from " +
                                                  shape().length() +
                                                  " to " +
                                                  newShape.length());
      }
      SparseStringNDArray temp = new SparseStringNDArray(newShape);
      forEachSparse((index, value) -> {
         temp.set((int) index, value);
      });
      this.data = temp.data;
      shape().reshape(newShape);
      return this;
   }

   @Override
   public ObjectNDArray<String> set(int kernel, int channel, int row, int col, Object value) {
      int slice = shape().calculateSliceIndex(kernel, channel);
      int matrix = shape().calculateMatrixIndex(row, col);
      if (value == null) {
         data[slice].removeKey(matrix);
      } else {
         data[slice].put(matrix, value.toString());
      }
      return this;
   }


   @Override
   public long size() {
      return Arrays.stream(data).mapToLong(OpenIntObjectHashMap::size).sum();
   }

   @Override
   public ObjectNDArray<String> slice(int index) {
      SparseStringNDArray v = new SparseStringNDArray(Shape.shape(shape().rows(),
                                                                  shape().columns()));
      if (data.length == 1) {
         v.data[0] = data[0];
      } else {
         v.data[0] = data[index];
      }
      return v;
   }

   @Override
   public ObjectNDArray<String> slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      SparseStringNDArray v = new SparseStringNDArray(os);
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
   public String[] toArray() {
      String[] out = new String[(int) length()];
      forEachSparse((index, value) -> out[(int) index] = value);
      return out;
   }


}//END OF SparseStringNDArray
