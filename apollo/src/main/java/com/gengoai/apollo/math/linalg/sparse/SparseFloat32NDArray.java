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
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenIntFloatHashMap;
import org.tensorflow.Tensor;

import java.util.Arrays;

import static com.gengoai.Validation.checkArgument;

public class SparseFloat32NDArray extends NumericNDArray {
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

   @JsonCreator
   protected SparseFloat32NDArray(@JsonProperty("data") float[] data,
                                 @JsonProperty("shape") Shape shape,
                                 @JsonProperty("label") Object label,
                                 @JsonProperty("predicted") Object predicted,
                                 @JsonProperty("weight") double weight) {
      this(shape, data);
      setLabel(label);
      setPredicted(predicted);
      setWeight(weight);
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
   public NumericNDArray compact() {
      for (OpenIntFloatHashMap datum : data) {
         datum.trimToSize();
      }
      return this;
   }

   @Override
   public NumericNDArrayFactory factory() {
      return nd.SFLOAT32;
   }

   @Override
   public void forEachSparse(@NonNull EntryConsumer<Number> consumer) {
      for (int i = 0; i < data.length; i++) {
         int offset = i * shape().matrixLength();
         data[i].forEachPair((m, s) -> {
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
   public NumericNDArray reshape(@NonNull Shape newShape) {
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
   public NumericNDArray set(int kernel, int channel, int row, int col, @NonNull Object value) {
      checkArgument(value instanceof Number, () -> "Cannot fill NumericNDArray with '" + value.getClass()
                                                                                              .getSimpleName() + "' value.");
      return set(kernel, channel, row, col, Cast.as(value, Number.class).doubleValue());
   }

   @Override
   public NumericNDArray set(int kernel, int channel, int row, int col, double value) {
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
   public NumericNDArray slice(int index) {
      SparseFloat32NDArray v = new SparseFloat32NDArray(Shape.shape(shape().rows(),
                                                                    shape().columns()));
      if( data.length == 1 ) {
         v.data[0] = data[0];
      } else {
         v.data[0] = data[index];
      }
      return v;
   }

   @Override
   public NumericNDArray slice(int startKernel, int startChannel, int endKernel, int endChannel) {
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
   public double[] toDoubleArray() {
      double[] out = new double[(int) length()];
      forEachSparse((index, value) -> out[(int) index] = value.doubleValue());
      return out;
   }

   @JsonProperty("data")
   public float[] toFloatArray() {
      float[] out = new float[(int) length()];
      forEachSparse((index, value) -> out[(int) index] = value.floatValue());
      return out;
   }

   @Override
   public Tensor<?> toTensor() {
      return nd.DFLOAT32.array(shape(), toFloatArray()).toTensor();
   }

}
