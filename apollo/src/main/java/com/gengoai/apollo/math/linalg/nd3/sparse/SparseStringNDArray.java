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

package com.gengoai.apollo.math.linalg.nd3.sparse;

import com.gengoai.apollo.math.linalg.nd3.*;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.tensorflow.Tensor;

import java.util.Arrays;

public class SparseStringNDArray extends NDArray<String> {
   private OpenIntObjectHashMap<String>[] data;

   /**
    * Instantiates a new Nd array.
    *
    * @param shape the shape
    */
   @SuppressWarnings("unchecked")
   public SparseStringNDArray(Shape shape) {
      super(shape);
      this.data = new OpenIntObjectHashMap[shape.sliceLength()];
      for (int i = 0; i < shape.sliceLength(); i++) {
         this.data[i] = new OpenIntObjectHashMap<>();
      }
   }

   public SparseStringNDArray(@NonNull String[] v) {
      this(Shape.shape(v.length));
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }

   public SparseStringNDArray(@NonNull Shape shape, @NonNull String[] v) {
      this(shape);
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }

   public SparseStringNDArray(@NonNull String[][] v) {
      this(Shape.shape(v.length, v[0].length));
      for (int row = 0; row < v.length; row++) {
         for (int col = 0; col < v[row].length; col++) {
            set(row, col, v[row][col]);
         }
      }
   }

   public SparseStringNDArray(@NonNull String[][][] v) {
      this(Shape.shape(v.length, v[0].length, v[0][0].length));
      for (int channel = 0; channel < v.length; channel++) {
         for (int row = 0; row < v[channel].length; row++) {
            for (int col = 0; col < v[channel][row].length; col++) {
               set(channel, row, col, v[channel][row][col]);
            }
         }
      }
   }

   public SparseStringNDArray(@NonNull String[][][][] v) {
      this(Shape.shape(v.length, v[0].length, v[0][0].length, v[0][0][0].length));
      for (int kernel = 0; kernel < v.length; kernel++) {
         for (int channel = 0; channel < v[kernel].length; channel++) {
            for (int row = 0; row < v[kernel][channel].length; row++) {
               for (int col = 0; col < v[kernel][channel][row].length; col++) {
                  set(kernel, channel, row, col, v[kernel][channel][row][col]);
               }
            }
         }
      }
   }

   @Override
   public NDArray<String> compact() {
      for (OpenIntObjectHashMap<String> datum : data) {
         datum.trimToSize();
      }
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj == this) {
         return true;
      }
      if (obj instanceof NDArray) {
         NDArray<?> r = Cast.as(obj);
         if (r.getType() == String.class && shape().equals(r.shape())) {
            for (Index index : shape().range()) {
               if (!get(index).equals(r.get(index))) {
                  return false;
               }
            }
            return true;
         }
      }
      return false;
   }

   @Override
   public NDArrayFactory<String> factory() {
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
   public double getDouble(int kernel, int channel, int row, int col) {
      throw new IllegalStateException(String.format(Validator.THIS_NUMERIC, getType().getSimpleName()));
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
   public boolean isNumeric() {
      return false;
   }

   @Override
   public NDArray<String> reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException();
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
   public NDArray<String> set(int kernel, int channel, int row, int col, String value) {
      int slice = shape().calculateSliceIndex(kernel, channel);
      int matrix = shape().calculateMatrixIndex(row, col);
      if (value == null) {
         data[slice].removeKey(matrix);
      } else {
         data[slice].put(matrix, value);
      }
      return this;
   }

   @Override
   public NDArray<String> set(int kernel, int channel, int row, int col, double value) {
      throw new IllegalStateException(String.format(Validator.THIS_NUMERIC, getType().getSimpleName()));
   }


   @Override
   public long size() {
      return Arrays.stream(data).mapToLong(OpenIntObjectHashMap::size).sum();
   }

   @Override
   public NDArray<String> slice(int index) {
      SparseStringNDArray v = new SparseStringNDArray(Shape.shape(shape().rows(),
                                                                  shape().columns()));
      v.data[0] = data[index];
      return v;
   }

   @Override
   public NDArray<String> slice(int startKernel, int startChannel, int endKernel, int endChannel) {
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
   public Tensor<?> toTensor() {
      return nd.DSTRING.array(shape(), arrayForTensor()).toTensor();
   }


}
