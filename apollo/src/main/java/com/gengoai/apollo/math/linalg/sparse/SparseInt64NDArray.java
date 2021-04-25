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
import org.apache.mahout.math.map.OpenIntLongHashMap;
import org.tensorflow.Tensor;

import java.util.Arrays;

import static com.gengoai.Validation.checkArgument;

/**
 * <p>Sparse NDArray representing 64-bit int values.</p>
 *
 * @author David B. Bracewell
 */
public class SparseInt64NDArray extends Int64NDArray {
   private static final long serialVersionUID = 1L;
   private OpenIntLongHashMap[] data;

   protected SparseInt64NDArray(Shape shape) {
      super(shape);
      this.data = new OpenIntLongHashMap[shape.sliceLength()];
      for (int i = 0; i < shape.sliceLength(); i++) {
         this.data[i] = new OpenIntLongHashMap();
      }
   }

   @JsonCreator
   protected SparseInt64NDArray(@JsonProperty("data") long[] data,
                                @JsonProperty("shape") Shape shape,
                                @JsonProperty("label") Object label,
                                @JsonProperty("predicted") Object predicted,
                                @JsonProperty("weight") double weight) {
      this(shape, data);
      setLabel(label);
      setPredicted(predicted);
      setWeight(weight);
   }


   protected SparseInt64NDArray(@NonNull Shape shape, @NonNull long[] v) {
      this(shape);
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }


   @Override
   public NumericNDArray compact() {
      for (OpenIntLongHashMap datum : data) {
         datum.trimToSize();
      }
      return this;
   }

   @Override
   public NumericNDArrayFactory factory() {
      return nd.SINT64;
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
   public Long get(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)].get(shape().calculateMatrixIndex(row, col));
   }

   @Override
   public double getDouble(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)].get(shape().calculateMatrixIndex(row, col));
   }

   @Override
   public Class<?> getType() {
      return Long.class;
   }

   @Override
   public boolean isDense() {
      return false;
   }

   @Override
   public NumericNDArray reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException();
      }
      SparseInt64NDArray temp = new SparseInt64NDArray(newShape);
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
         data[slice].put(matrix, (long) value);
      }
      return this;
   }

   @Override
   public long size() {
      return Arrays.stream(data).mapToLong(OpenIntLongHashMap::size).sum();
   }

   @Override
   public NumericNDArray slice(int index) {
      SparseInt64NDArray v = new SparseInt64NDArray(Shape.shape(shape().rows(),
                                                                shape().columns()));
      if (data.length == 1) {
         v.data[0] = data[0];
      } else {
         v.data[0] = data[index];
      }
      return v;
   }

   @Override
   public NumericNDArray slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      SparseInt64NDArray v = new SparseInt64NDArray(os);
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
   public float[] toFloatArray() {
      float[] out = new float[(int) length()];
      forEachSparse((index, value) -> out[(int) index] = value.floatValue());
      return out;
   }

   @Override
   public int[] toIntArray() {
      int[] out = new int[(int) length()];
      forEachSparse((index, value) -> out[(int) index] = value.intValue());
      return out;
   }

   @Override
   public double[] toDoubleArray() {
      double[] out = new double[(int) length()];
      forEachSparse((index, value) -> out[(int) index] = value.doubleValue());
      return out;
   }

   @Override
   @JsonProperty("data")
   public long[] toLongArray() {
      long[] out = new long[(int) length()];
      forEachSparse((index, value) -> out[(int) index] = value.longValue());
      return out;
   }


}//END OF SparseInt64NDArray
