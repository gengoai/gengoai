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

package com.gengoai.apollo.math.linalg.nd3.dense;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.nd3.Index;
import com.gengoai.apollo.math.linalg.nd3.NDArray;
import com.gengoai.apollo.math.linalg.nd3.Shape;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;

public class DenseInt64NDArray extends NDArray<Long> {
   private static final long serialVersionUID = 1L;
   private long[][] data;

   /**
    * Instantiates a new Nd array.
    *
    * @param shape the shape
    */
   public DenseInt64NDArray(Shape shape) {
      super(shape);
      this.data = new long[shape().sliceLength()][shape().matrixLength()];
   }

   public DenseInt64NDArray(@NonNull long[] v) {
      super(Shape.shape(v.length));
      this.data = new long[1][v.length];
      System.arraycopy(v, 0, this.data[0], 0, v.length);
   }

   public DenseInt64NDArray(@NonNull Shape shape, @NonNull long[] v) {
      super(shape);
      this.data = new long[1][v.length];
      System.arraycopy(v, 0, this.data[0], 0, v.length);
   }

   public DenseInt64NDArray(@NonNull long[][] v) {
      super(Shape.shape(v.length, v[0].length));
      this.data = new long[1][shape().matrixLength()];
      for (int row = 0; row < v.length; row++) {
         for (int col = 0; col < v[row].length; col++) {
            set(row,col, v[row][col]);
         }
      }
   }

   public DenseInt64NDArray(@NonNull long[][][] v) {
      super(Shape.shape(v.length, v[0].length, v[0][0].length));
      this.data = new long[shape().sliceLength()][shape().matrixLength()];
      for (int channel = 0; channel < v.length; channel++) {
         for (int row = 0; row < v[channel].length; row++) {
            for (int col = 0; col < v[channel][row].length; col++) {
               set(channel,row,col, v[channel][row][col]);
            }
         }
      }
   }

   public DenseInt64NDArray(@NonNull long[][][][] v) {
      super(Shape.shape(v.length, v[0].length, v[0][0].length, v[0][0][0].length));
      this.data = new long[shape().sliceLength()][shape().matrixLength()];
      for (int kernel = 0; kernel < v.length; kernel++) {
         for (int channel = 0; channel < v[kernel].length; channel++) {
            for (int row = 0; row < v[kernel][channel].length; row++) {
               for (int col = 0; col < v[kernel][channel][row].length; col++) {
                  set(kernel,channel,row,col, v[kernel][channel][row][col]);
               }
            }
         }
      }
   }

   public static NDArray<Long> fromTensor(@NonNull Tensor<?> tensor) {
      if (tensor.dataType() == DataType.INT64) {
         Shape s = Shape.shape(tensor.shape());
         switch (s.rank()) {
            case 1:
               return new DenseInt64NDArray(tensor.copyTo(new long[s.columns()]));
            case 2:
               return new DenseInt64NDArray(tensor.copyTo(new long[s.rows()][s.columns()]));
            case 3:
               return new DenseInt64NDArray(tensor.copyTo(new long[s.channels()][s.rows()][s.columns()]));
            default:
               return new DenseInt64NDArray(tensor.copyTo(new long[s.kernels()][s.channels()][s.rows()][s
                     .columns()]));
         }
      }
      throw new IllegalArgumentException("Unsupported type '" + tensor.dataType().name() + "'");
   }

   @Override
   public NDArray<Long> reshape(@NonNull Shape newShape) {
      if( shape().length() != newShape.length()){
         throw new IllegalArgumentException();
      }
      long[][] temp = new long[newShape.sliceLength()][newShape.matrixLength()];
      for (int i = 0; i < length(); i++) {
         long v = get(i);
         int sliceIndex = newShape.toSliceIndex(i);
         int matrixIndex = newShape.toMatrixIndex(i);
         temp[sliceIndex][matrixIndex] = v;
      }
      this.data = temp;
      shape().reshape(newShape);
      return this;
   }

   @Override
   public Long get(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)];
   }

   @Override
   public double getDouble(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)];
   }

   @Override
   public Class<?> getType() {
      return Long.class;
   }

   @Override
   public boolean isDense() {
      return true;
   }

   @Override
   public boolean isNumeric() {
      return true;
   }


   @Override
   public NDArray<Long> set(int kernel, int channel, int row, int col, @NonNull Long value) {
      data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)] = value;
      return this;
   }

   @Override
   public NDArray<Long> set(int kernel, int channel, int row, int col, double value) {
      data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)] = (long) value;
      return this;
   }


   @Override
   public NDArray<Long> setSlice(int index, @NonNull NDArray<Long> slice) {
      if (!slice.shape().equals(shape().matrixShape())) {
         throw new IllegalArgumentException("Unable to set slice of different shape");
      }
      if (slice instanceof DenseInt64NDArray) {
         DenseInt64NDArray m = Cast.as(slice);
         System.arraycopy(m.data[0], 0, data[index], 0, (int)slice.length());
         return this;
      }
      return super.setSlice(index, slice);
   }

   @Override
   public NDArray<Long> slice(int index) {
      DenseInt64NDArray v = new DenseInt64NDArray(Shape.shape(shape().rows(),
                                                              shape().columns()));
      v.data[0] = data[index];
      return v;
   }

   @Override
   public NDArray<Long> slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      DenseInt64NDArray v = new DenseInt64NDArray(os);
      for (int kernel = startKernel; kernel < endKernel; kernel++) {
         for (int channel = startChannel; channel < endChannel; channel++) {
            int ti = shape().calculateSliceIndex(kernel, channel);
            int oi = os.calculateSliceIndex(kernel - startKernel, channel - startChannel);
            v.data[oi] = data[ti];
         }
      }
      return v;
   }


}//END OF DenseInt64NDArray
