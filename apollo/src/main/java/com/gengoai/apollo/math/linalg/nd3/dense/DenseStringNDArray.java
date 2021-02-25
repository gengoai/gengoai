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

import com.gengoai.apollo.math.linalg.nd3.Index;
import com.gengoai.apollo.math.linalg.nd3.NDArray;
import com.gengoai.apollo.math.linalg.nd3.Shape;
import com.gengoai.apollo.math.linalg.nd3.Validator;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;

public class DenseStringNDArray extends NDArray<String> {
   private static final long serialVersionUID = 1L;
   private String[][] data;


   /**
    * Instantiates a new Nd array.
    *
    * @param shape the shape
    */
   public DenseStringNDArray(Shape shape) {
      super(shape);
      this.data = new String[shape().sliceLength()][shape().matrixLength()];
   }

   public DenseStringNDArray(@NonNull String[] v) {
      super(Shape.shape(v.length));
      this.data = new String[1][v.length];
      System.arraycopy(v, 0, this.data[0], 0, v.length);
   }

   public DenseStringNDArray(@NonNull Shape shape, @NonNull String[] v) {
      super(shape);
      this.data = new String[1][v.length];
      System.arraycopy(v, 0, this.data[0], 0, v.length);
   }

   public DenseStringNDArray(@NonNull String[][] v) {
      super(Shape.shape(v.length, v[0].length));
      this.data = new String[1][shape().matrixLength()];
      for (int row = 0; row < v.length; row++) {
         for (int col = 0; col < v[row].length; col++) {
            set(row,col, v[row][col]);
         }
      }
   }

   public DenseStringNDArray(@NonNull String[][][] v) {
      super(Shape.shape(v.length, v[0].length, v[0][0].length));
      this.data = new String[shape().sliceLength()][shape().matrixLength()];
      for (int channel = 0; channel < v.length; channel++) {
         for (int row = 0; row < v[channel].length; row++) {
            for (int col = 0; col < v[channel][row].length; col++) {
               set(channel,row,col, v[channel][row][col]);
            }
         }
      }
   }

   public DenseStringNDArray(@NonNull String[][][][] v) {
      super(Shape.shape(v.length, v[0].length, v[0][0].length, v[0][0][0].length));
      this.data = new String[shape().sliceLength()][shape().matrixLength()];
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

   public DenseStringNDArray(@NonNull byte[][] v) {
      super(Shape.shape(v.length));
      this.data = new String[1][shape().matrixLength()];
      for (int i = 0; i < v.length; i++) {
         this.data[0][i] = new String(v[i]);
      }
   }

   public DenseStringNDArray(@NonNull byte[][][] v) {
      super(Shape.shape(v.length, v[0].length));
      this.data = new String[1][shape().matrixLength()];
      for (int row = 0; row < v.length; row++) {
         for (int col = 0; col < v[row].length; col++) {
            this.data[0][shape().calculateMatrixIndex(row, col)] = new String(v[row][col]);
         }
      }
   }

   public DenseStringNDArray(@NonNull byte[][][][] v) {
      super(Shape.shape(v.length, v[0].length, v[0][0].length));
      this.data = new String[shape().sliceLength()][shape().matrixLength()];
      for (int channel = 0; channel < v.length; channel++) {
         for (int row = 0; row < v[channel].length; row++) {
            for (int col = 0; col < v[channel][row].length; col++) {
               this.data[channel][shape().calculateMatrixIndex(row, col)] = new String(v[channel][row][col]);
            }
         }
      }
   }

   public DenseStringNDArray(@NonNull byte[][][][][] v) {
      super(Shape.shape(v.length, v[0].length, v[0][0].length, v[0][0][0].length));
      this.data = new String[shape().sliceLength()][shape().matrixLength()];
      for (int kernel = 0; kernel < v.length; kernel++) {
         for (int channel = 0; channel < v[kernel].length; channel++) {
            for (int row = 0; row < v[kernel][channel].length; row++) {
               for (int col = 0; col < v[kernel][channel][row].length; col++) {
                  this.data[shape().calculateSliceIndex(kernel, channel)][shape()
                        .calculateMatrixIndex(row, col)] = new String(v[kernel][channel][row][col]);
               }
            }
         }
      }
   }

   public static NDArray<String> fromTensor(@NonNull Tensor<?> tensor) {
      if (tensor.dataType() == DataType.STRING) {
         long[] s = tensor.shape();
         switch (s.length) {
            case 1:
               return new DenseStringNDArray(tensor.copyTo(new byte[(int) s[0]][]));
            case 2:
               return new DenseStringNDArray(tensor.copyTo(new byte[(int) s[0]][(int) s[1]][]));
            case 3:
               return new DenseStringNDArray(tensor.copyTo(new byte[(int) s[0]][(int) s[1]][(int) s[2]][]));
            case 4:
               return new DenseStringNDArray(tensor.copyTo(new byte[(int) s[0]][(int) s[1]][(int) s[2]][(int) s[3]][]));
         }
      }
      throw new IllegalArgumentException("Unsupported type '" + tensor.dataType().name() + "'");
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
   public String get(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)];
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
      return true;
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
      String[][] temp = new String[newShape.sliceLength()][newShape.matrixLength()];
      for (int i = 0; i < length(); i++) {
         String v = get(i);
         int sliceIndex = newShape.toSliceIndex(i);
         int matrixIndex = newShape.toMatrixIndex(i);
         temp[sliceIndex][matrixIndex] = v;
      }
      this.data = temp;
      shape().reshape(newShape);
      return this;
   }

   @Override
   public NDArray<String> set(int kernel, int channel, int row, int col, String value) {
      data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)] = value;
      return this;
   }

   @Override
   public NDArray<String> set(int kernel, int channel, int row, int col, double value) {
      throw new IllegalStateException(String.format(Validator.THIS_NUMERIC, getType().getSimpleName()));
   }


   @Override
   public NDArray<String> setSlice(int index, @NonNull NDArray<String> slice) {
      if (!slice.shape().equals(shape().matrixShape())) {
         throw new IllegalArgumentException("Unable to set slice of different shape");
      }
      if (slice instanceof DenseStringNDArray) {
         DenseStringNDArray m = Cast.as(slice);
         System.arraycopy(m.data[0], 0, data[index], 0, (int)slice.length());
         return this;
      }
      return super.setSlice(index, slice);
   }

   @Override
   public NDArray<String> slice(int index) {
      DenseStringNDArray v = new DenseStringNDArray(Shape.shape(shape().rows(),
                                                              shape().columns()));
      v.data[0] = data[index];
      return v;
   }

   @Override
   public NDArray<String> slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      DenseStringNDArray v = new DenseStringNDArray(os);
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
   public Tensor<?> toTensor() {

      if (shape().rank() == 0) {
         return Tensor.create(new byte[0][0]);
      }

      if (shape().rank() == 1) {
         byte[][] b = new byte[(int) length()][];
         for (int i = 0; i < data[0].length; i++) {
            b[i] = data[0][i].getBytes();
         }
         return Tensor.create(b);
      }

      if (shape().rank() == 2) {
         byte[][][] b = new byte[(int) shape().rows()][(int) shape().columns()][];
         for (int row = 0; row < shape().rows(); row++) {
            for (int col = 0; col < shape().columns(); col++) {
               b[row][col] = get(row, col).getBytes();
            }
         }
         return Tensor.create(b);
      }

      if (shape().rank() == 3) {
         byte[][][][] b = new byte[(int) shape().channels()][(int) shape().rows()][(int) shape().columns()][];
         for (int channel = 0; channel < shape().channels(); channel++) {
            for (int row = 0; row < shape().rows(); row++) {
               for (int col = 0; col < shape().columns(); col++) {
                  b[channel][row][col] = data[channel][shape().calculateMatrixIndex(row, col)].getBytes();
               }
            }
         }
         return Tensor.create(b);
      }

      if (shape().rank() == 4) {
         byte[][][][][] b = new byte[(int) shape().kernels()][(int) shape().channels()][(int) shape()
               .rows()][(int) shape().columns()][];
         for (int kernel = 0; kernel < shape().kernels(); kernel++) {
            for (int channel = 0; channel < shape().channels(); channel++) {
               int sliceIndex = shape().calculateSliceIndex(kernel, channel);
               for (int row = 0; row < shape().rows(); row++) {
                  for (int col = 0; col < shape().columns(); col++) {
                     b[kernel][channel][row][col] = data[sliceIndex][shape().calculateMatrixIndex(row, col)].getBytes();
                  }
               }
            }
         }
         return Tensor.create(b);
      }

      throw new IllegalStateException();
   }

}
