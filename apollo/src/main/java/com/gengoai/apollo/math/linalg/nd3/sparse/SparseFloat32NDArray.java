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

import com.gengoai.apollo.math.linalg.nd3.NDArray;
import com.gengoai.apollo.math.linalg.nd3.NDArrayFactory;
import com.gengoai.apollo.math.linalg.nd3.Shape;
import com.gengoai.apollo.math.linalg.nd3.nd;
import lombok.NonNull;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenIntFloatHashMap;
import org.tensorflow.Tensor;

import java.util.Arrays;

public class SparseFloat32NDArray extends NDArray<Float> {
   private OpenIntFloatHashMap[] data;

   /**
    * Instantiates a new Nd array.
    *
    * @param shape the shape
    */
   public SparseFloat32NDArray(Shape shape) {
      super(shape);
      this.data = new OpenIntFloatHashMap[shape.sliceLength()];
      for (int i = 0; i < shape.sliceLength(); i++) {
         this.data[i] = new OpenIntFloatHashMap();
      }
   }

   public SparseFloat32NDArray(@NonNull float[] v) {
      this(Shape.shape(v.length));
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }


   public SparseFloat32NDArray(@NonNull Shape shape, @NonNull float[] v) {
      this(shape);
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }

   public SparseFloat32NDArray(@NonNull float[][] v) {
      this(Shape.shape(v.length, v[0].length));
      for (int row = 0; row < v.length; row++) {
         for (int col = 0; col < v[row].length; col++) {
            set(row, col, v[row][col]);
         }
      }
   }

   public SparseFloat32NDArray(@NonNull float[][][] v) {
      this(Shape.shape(v.length, v[0].length, v[0][0].length));
      for (int channel = 0; channel < v.length; channel++) {
         for (int row = 0; row < v[channel].length; row++) {
            for (int col = 0; col < v[channel][row].length; col++) {
               set(channel, row, col, v[channel][row][col]);
            }
         }
      }
   }

   public SparseFloat32NDArray(@NonNull float[][][][] v) {
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
   public NDArray<Float> compact() {
      for (OpenIntFloatHashMap datum : data) {
         datum.trimToSize();
      }
      return this;
   }

   @Override
   public NDArrayFactory<Float> factory() {
      return nd.SFLOAT32;
   }

   @Override
   public void forEachSparse(@NonNull EntryConsumer<Float> consumer) {
      for (int i = 0; i < data.length; i++) {
         OpenIntFloatHashMap map = data[i];
         int offset = i * shape().matrixLength();
         map.forEachPair((m, s) -> {
            consumer.apply(offset + m, s);
            return true;
         });
      }
   }

   @Override
   public Float get(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)].get(shape().calculateMatrixIndex(row, col));
   }

   @Override
   public double getDouble(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)].get(shape().calculateMatrixIndex(row, col));
   }

   @Override
   public Class<?> getType() {
      return Float.class;
   }

   @Override
   public boolean isDense() {
      return false;
   }

   @Override
   public boolean isNumeric() {
      return true;
   }

   @Override
   public NDArray<Float> reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException();
      }
      SparseFloat32NDArray temp = new SparseFloat32NDArray(newShape);
      forEachSparse((index, value) -> {
         temp.set((int) index, value);
      });
      this.data = temp.data;
      shape().reshape(newShape);
      return this;
   }

   @Override
   public NDArray<Float> set(int kernel, int channel, int row, int col, @NonNull Float value) {
      return set(kernel, channel, row, col, value.doubleValue());
   }

   @Override
   public NDArray<Float> set(int kernel, int channel, int row, int col, double value) {
      int slice = shape().calculateSliceIndex(kernel, channel);
      int matrix = shape().calculateMatrixIndex(row, col);
      if (value == 0) {
         data[slice].removeKey(matrix);
      } else {
         data[slice].put(matrix, (float) value);
      }
      return this;
   }

   @Override
   public long size() {
      return Arrays.stream(data).mapToLong(OpenIntFloatHashMap::size).sum();
   }

   @Override
   public NDArray<Float> slice(int index) {
      SparseFloat32NDArray v = new SparseFloat32NDArray(Shape.shape(shape().rows(),
                                                                    shape().columns()));
      v.data[0] = data[index];
      return v;
   }

   @Override
   public NDArray<Float> slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      SparseFloat32NDArray v = new SparseFloat32NDArray(os);
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
      return nd.DFLOAT32.array(shape(), arrayForTensor()).toTensor();
   }

}
