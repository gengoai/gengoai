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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.apollo.math.linalg.*;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.tensorflow.ndarray.IntNdArray;

import java.util.Arrays;

import static com.gengoai.Validation.checkArgument;

/**
 * <p>Dense NDArray representing 32-bit int values.</p>
 *
 * @author David B. Bracewell
 */
public class DenseInt32NDArray extends Int32NDArray {
   private static final long serialVersionUID = 1L;
   private int[][] data;

   @JsonCreator
   protected DenseInt32NDArray(@JsonProperty("data") int[] data,
                               @JsonProperty("shape") Shape shape,
                               @JsonProperty("label") Object label,
                               @JsonProperty("predicted") Object predicted,
                               @JsonProperty("weight") double weight) {
      this(shape, data);
      setLabel(label);
      setPredicted(predicted);
      setWeight(weight);
   }

   protected DenseInt32NDArray(@NonNull Shape shape) {
      super(shape);
      this.data = new int[shape.sliceLength()][shape.matrixLength()];
   }

   protected DenseInt32NDArray(@NonNull int[] v) {
      super(Shape.shape(v.length));
      this.data = new int[1][v.length];
      System.arraycopy(v, 0, this.data[0], 0, v.length);
   }

   protected DenseInt32NDArray(@NonNull Shape shape, @NonNull int[] v) {
      super(shape);
      this.data = new int[shape.sliceLength()][shape.matrixLength()];
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }

   /**
    * <p>Converts TensorFlow Tenors for INT32 type to DenseFloat32NDArray.</p>
    *
    * @param ndarray the tensor
    * @return the converted Tensor
    */
   public static NumericNDArray fromTensor(@NonNull IntNdArray ndarray) {
      NumericNDArray rval = nd.DINT32.zeros(Shape.shape(ndarray.shape().asArray()));
      ndarray.scalars().forEachIndexed((coords, value) -> rval.set(coords, value.getInt()));
      return rval;
   }

   @Override
   public Integer get(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)];
   }

   @Override
   public double getDouble(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)];
   }

   @Override
   public Class<?> getType() {
      return Integer.class;
   }

   @Override
   public boolean isDense() {
      return true;
   }

   @Override
   public NumericNDArray reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException("Cannot change the total number of elements from " +
                                            shape().length() +
                                            " to " +
                                            newShape.length() +
                                            " on reshape");
      }
      int[][] temp = new int[newShape.sliceLength()][newShape.matrixLength()];
      for (int i = 0; i < length(); i++) {
         double v = getDouble(i);
         int sliceIndex = newShape.toSliceIndex(i);
         int matrixIndex = newShape.toMatrixIndex(i);
         temp[sliceIndex][matrixIndex] = (int) v;
      }
      this.data = temp;
      shape().reshape(newShape);
      return this;
   }

   @Override
   public NumericNDArray set(int kernel, int channel, int row, int col, @NonNull Object value) {
      checkArgument(value instanceof Number, () -> "Cannot fill NumericNDArray with '" + value.getClass()
                                                                                              .getSimpleName() + "' value.");
      data[shape().calculateSliceIndex(kernel, channel)]
            [shape().calculateMatrixIndex(row, col)] = Cast.as(value, Number.class).intValue();
      return this;
   }

   @Override
   public NumericNDArray set(int kernel, int channel, int row, int col, double value) {
      data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)] = (int) value;
      return this;
   }

   @Override
   public NumericNDArray setSlice(int index, @NonNull NDArray slice) {
      if (!slice.shape().equals(shape().matrixShape())) {
         throw new IllegalArgumentException("Unable to set slice of different shape");
      }
      if (slice instanceof DenseInt32NDArray) {
         DenseInt32NDArray m = Cast.as(slice);
         System.arraycopy(m.data[0], 0, data[index], 0, (int) slice.length());
         return this;
      }
      return super.setSlice(index, slice);
   }

   @Override
   public NumericNDArray slice(int index) {
      DenseInt32NDArray v = new DenseInt32NDArray(Shape.shape(shape().rows(), shape().columns()));
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
      DenseInt32NDArray v = new DenseInt32NDArray(os);
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
   @JsonProperty("data")
   public int[] toIntArray() {
      int[] array = new int[(int) length()];
      for (int i = 0; i < data.length; i++) {
         System.arraycopy(data[i], 0, array, i * shape().matrixLength(), shape().matrixLength());
      }
      return array;
   }

}//END OF DenseInt32NDArray
