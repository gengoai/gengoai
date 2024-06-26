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
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.proto.DataType;
import org.tensorflow.types.TString;

/**
 * <p>Dense NDArray representing String values.</p>
 *
 * @author David B. Bracewell
 */
public class DenseStringNDArray extends StringNDArray {
   private static final long serialVersionUID = 1L;
   private String[][] data;


   /**
    * Instantiates a new Nd array.
    *
    * @param shape the shape
    */
   protected DenseStringNDArray(Shape shape) {
      super(shape);
      this.data = new String[shape().sliceLength()][shape().matrixLength()];
   }


   @JsonCreator
   protected DenseStringNDArray(@JsonProperty("data") String[] data,
                                @JsonProperty("shape") Shape shape,
                                @JsonProperty("label") Object label,
                                @JsonProperty("predicted") Object predicted,
                                @JsonProperty("weight") double weight) {
      this(shape, data);
      setLabel(label);
      setPredicted(predicted);
      setWeight(weight);
   }

   protected DenseStringNDArray(@NonNull String[] v) {
      super(Shape.shape(v.length));
      this.data = new String[1][v.length];
      System.arraycopy(v, 0, this.data[0], 0, v.length);
   }

   protected DenseStringNDArray(@NonNull Shape shape, @NonNull String[] v) {
      super(shape);
      this.data = new String[shape.sliceLength()][shape.matrixLength()];
      for (int i = 0; i < v.length; i++) {
         set(i, v[i]);
      }
   }

   protected DenseStringNDArray(@NonNull byte[][] v) {
      super(Shape.shape(v.length));
      this.data = new String[1][shape().matrixLength()];
      for (int i = 0; i < v.length; i++) {
         this.data[0][i] = new String(v[i]);
      }
   }

   protected DenseStringNDArray(@NonNull byte[][][] v) {
      super(Shape.shape(v.length, v[0].length));
      this.data = new String[1][shape().matrixLength()];
      for (int row = 0; row < v.length; row++) {
         for (int col = 0; col < v[row].length; col++) {
            this.data[0][shape().calculateMatrixIndex(row, col)] = new String(v[row][col]);
         }
      }
   }

   protected DenseStringNDArray(@NonNull byte[][][][] v) {
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

   protected DenseStringNDArray(@NonNull byte[][][][][] v) {
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

   /**
    * <p>Converts TensorFlow Tensors for STRING type to DenseFloat32NDArray.</p>
    *
    * @param tensor the tensor
    * @return the converted Tensor
    */
   public static ObjectNDArray<String> fromTensor(@NonNull Tensor tensor) {
      if (tensor.dataType() == DataType.DT_STRING) {
         TString ndarray = Cast.as(tensor);
         ObjectNDArray<String> rval = nd.DSTRING.zeros(Shape.shape(tensor.shape().asArray()));
         ndarray.scalars().forEachIndexed((coords, value) -> rval.set(coords, value.getObject()));
      }
      throw new IllegalArgumentException("Unsupported type '" + tensor.dataType().name() + "'");
   }

   @Override
   public String get(int kernel, int channel, int row, int col) {
      return data[shape().calculateSliceIndex(kernel, channel)][shape().calculateMatrixIndex(row, col)];
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
   public ObjectNDArray<String> reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException("Cannot change total length from " +
                                            shape().length() +
                                            " to " +
                                            newShape.length());
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
   public ObjectNDArray<String> set(int kernel, int channel, int row, int col, Object value) {
      data[shape().calculateSliceIndex(kernel, channel)]
            [shape().calculateMatrixIndex(row, col)] = value == null ? null : value.toString();
      return this;
   }

   @Override
   public ObjectNDArray<String> setSlice(int index, @NonNull NDArray slice) {
      if (!slice.shape().equals(shape().matrixShape())) {
         throw new IllegalArgumentException("Unable to set slice of different shape");
      }
      if (slice instanceof DenseStringNDArray) {
         DenseStringNDArray m = Cast.as(slice);
         System.arraycopy(m.data[0], 0, data[index], 0, (int) slice.length());
         return this;
      }
      return super.setSlice(index, slice);
   }

   @Override
   public ObjectNDArray<String> slice(int index) {
      DenseStringNDArray v = new DenseStringNDArray(Shape.shape(shape().rows(),
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
   @JsonProperty("data")
   public String[] toArray() {
      String[] out = new String[(int) length()];
      for (long i = 0; i < length(); i++) {
         out[(int) i] = get(i);
      }
      return out;
   }

   @Override
   public Tensor toTensor() {

      if (shape().rank() == 0) {
         return Cast.as(StdArrays.ndCopyOf(new byte[0]));
      }

      if (shape().rank() == 1) {
         byte[][] b = new byte[(int) length()][];
         for (int i = 0; i < data[0].length; i++) {
            b[i] = data[0][i].getBytes();
         }
         return Cast.as(StdArrays.ndCopyOf(b));
      }

      if (shape().rank() == 2) {
         byte[][][] b = new byte[(int) shape().rows()][(int) shape().columns()][];
         for (int row = 0; row < shape().rows(); row++) {
            for (int col = 0; col < shape().columns(); col++) {
               b[row][col] = get(row, col).getBytes();
            }
         }
         return Cast.as(StdArrays.ndCopyOf(b));
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
         return Cast.as(StdArrays.ndCopyOf(b));
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
         return Cast.as(StdArrays.ndCopyOf(b));
      }


//        TString tensor = TString.tensorOf(StandardCharsets.UTF_8,
//                                          org.tensorflow.ndarray.Shape.of(shape().toLongArray()),
//                                          DataBuffers.ofObjects(String.class, size()));
//        tensor.scalars().forEachIndexed((c, v) -> v.setObject(get(Index.index(c))));
//        return Cast.as(tensor.asBytes());

      throw new IllegalStateException();
   }
}//END OF DenseStringNDArray
