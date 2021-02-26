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

import com.gengoai.apollo.math.linalg.Index;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class DenseFloat32NDArray extends NDArray<Float> {
   private static final long serialVersionUID = 1L;
   private FloatMatrix[] matrices;

   public DenseFloat32NDArray(@NonNull Shape shape,
                              @NonNull float... data) {
      this(shape);
      for (int i = 0; i < data.length; i++) {
         set(i, data[i]);
      }
   }

   public DenseFloat32NDArray(@NonNull float... data) {
      super(Shape.shape(data.length));
      this.matrices = new FloatMatrix[]{new FloatMatrix(1, data.length, data)};
   }

   public DenseFloat32NDArray(@NonNull float[][] data) {
      super(Shape.shape(data.length, data[0].length));
      this.matrices = new FloatMatrix[]{new FloatMatrix(data)};
   }

   public DenseFloat32NDArray(@NonNull float[][][] data) {
      super(Shape.shape(data.length, data[0].length, data[0][0].length));
      this.matrices = new FloatMatrix[shape().sliceLength()];
      for (int channel = 0; channel < data.length; channel++) {
         this.matrices[channel] = new FloatMatrix(data[channel]);
      }
   }

   public DenseFloat32NDArray(@NonNull float[][][][] data) {
      super(Shape.shape(data.length, data[0].length, data[0][0].length, data[0][0][0].length));
      this.matrices = new FloatMatrix[shape().sliceLength()];
      for (int kernel = 0; kernel < data.length; kernel++) {
         for (int channel = 0; channel < data[kernel].length; channel++) {
            this.matrices[shape().calculateSliceIndex(kernel, channel)] = new FloatMatrix(data[kernel][channel]);
         }
      }
   }

   public DenseFloat32NDArray(@NonNull Shape shape) {
      super(shape);
      this.matrices = new FloatMatrix[shape.sliceLength()];
      for (int i = 0; i < this.matrices.length; i++) {
         this.matrices[i] = new FloatMatrix(Math.max(1, shape.rows()), shape.columns());
      }
   }

   public DenseFloat32NDArray(@NonNull FloatMatrix fm) {
      super(Shape.shape(fm.rows, fm.columns));
      this.matrices = new FloatMatrix[]{fm};
   }

   public DenseFloat32NDArray(@NonNull Shape shape, @NonNull FloatMatrix[] fm) {
      super(shape);
      this.matrices = fm;
   }

   public DenseFloat32NDArray(@NonNull int kernels, int channels, @NonNull FloatMatrix[] fm) {
      this(Shape.shape(kernels,channels,fm[0].rows, fm[0].columns),fm);
   }


   public static NDArray<Float> fromTensor(@NonNull Tensor<?> tensor) {
      if (tensor.dataType() == DataType.FLOAT) {
         Shape s = Shape.shape(tensor.shape());
         switch (s.rank()) {
            case 1:
               return new DenseFloat32NDArray(tensor.copyTo(new float[s.columns()]));
            case 2:
               return new DenseFloat32NDArray(tensor.copyTo(new float[s.rows()][s.columns()]));
            case 3:
               return new DenseFloat32NDArray(tensor.copyTo(new float[s.channels()][s.rows()][s.columns()]));
            default:
               return new DenseFloat32NDArray(tensor.copyTo(new float[s.kernels()][s.channels()][s.rows()][s
                     .columns()]));
         }
      }
      throw new IllegalArgumentException("Unsupported type '" + tensor.dataType().name() + "'");
   }

   @Override
   public NDArray<Float> T() {
      if (shape().isScalar() || shape().isEmpty()) {
         return copy();
      }
      return mapSlices(FloatMatrix::transpose);
   }

   @Override
   public NDArray<Float> fill(double value) {
      return forEachMatrix(value, (a, b) -> a.fill((float) value));
   }


   protected NDArray<Float> forEachMatrix(DenseFloat32NDArray rhs, BiMatrixConsumer op) {
      for (Index index : shape().sliceIterator()) {
         int ti = shape().calculateSliceIndex(index);
         int ri = rhs.shape().calculateSliceIndex(rhs.shape().broadcast(index));
         op.accept(matrices[ti], rhs.matrices[ri]);
      }
      return this;
   }

   protected NDArray<Float> forEachMatrix(double value, MatrixDoubleConsumer op) {
      for (FloatMatrix matrix : matrices) {
         op.accept(matrix, value);
      }
      return this;
   }

   @Override
   public Float get(int kernel, int channel, int row, int col) {
      return matrices[shape().calculateSliceIndex(kernel, channel)].get(row, col);
   }

   @Override
   public double getDouble(int kernel, int channel, int row, int col) {
      return matrices[shape().calculateSliceIndex(kernel, channel)].get(row, col);
   }

   @Override
   public Class<?> getType() {
      return Float.class;
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
   public NDArray<Float> map(@NonNull UnaryOperator<Float> operator) {
      return mapDouble((f) -> operator.apply((float) f));
   }

   @Override
   public NDArray<Float> map(@NonNull Float value, @NonNull BinaryOperator<Float> operator) {
      return mapDouble(value.doubleValue(), (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   public NDArray<Float> map(@NonNull NDArray<? extends Float> rhs, @NonNull BinaryOperator<Float> operator) {
      return mapDouble(rhs, (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   public NDArray<Float> mapAxis(int axis, int position, @NonNull Float value, @NonNull BinaryOperator<Float> operator) {
      return mapAxisDouble(axis, position, value.doubleValue(), (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   public NDArray<Float> mapAxis(int axis, int position, NDArray<? extends Float> rhs, @NonNull BinaryOperator<Float> operator) {
      return mapAxisDouble(axis, position, rhs, (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   public NDArray<Float> mapAxis(int axis, @NonNull NDArray<? extends Float> rhs, @NonNull BinaryOperator<Float> operator) {
      return mapiAxisDouble(axis, rhs, (a, b) -> operator.apply((float) a, (float) b));
   }

   protected NDArray<Float> mapSlices(UnaryOperator<FloatMatrix> op) {
      if (matrices.length == 0) {
         return factory().empty();
      }
      FloatMatrix[] fm = new FloatMatrix[matrices.length];
      for (int i = 0; i < matrices.length; i++) {
         fm[i] = op.apply(matrices[i]);
      }
      return new DenseFloat32NDArray(shape().with(Shape.ROW, fm[0].rows,
                                                Shape.COLUMN, fm[0].columns),
                                     fm);
   }

   @Override
   public NDArray<Float> mapi(@NonNull UnaryOperator<Float> operator) {
      return mapiDouble((f) -> operator.apply((float) f));
   }

   @Override
   public NDArray<Float> mapi(@NonNull Float value, @NonNull BinaryOperator<Float> operator) {
      return mapiDouble(value.doubleValue(), (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   public NDArray<Float> mapi(@NonNull NDArray<? extends Float> rhs, @NonNull BinaryOperator<Float> operator) {
      return mapiDouble(rhs, (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   public NDArray<Float> mapiAxis(int axis, @NonNull NDArray<? extends Float> rhs, @NonNull BinaryOperator<Float> operator) {
      return mapiAxisDouble(axis, rhs, (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   public NDArray<Float> mapiAxis(int axis, int position, @NonNull Float rhs, @NonNull BinaryOperator<Float> operator) {
      return mapiAxisDouble(axis, position, rhs, (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   public NDArray<Float> mapiAxis(int axis, int position, NDArray<? extends Float> rhs, @NonNull BinaryOperator<Float> operator) {
      return mapiAxisDouble(axis, position, rhs, (a, b) -> operator.apply((float) a, (float) b));
   }

   @Override
   protected NDArray<Float> matrixMultiplicationImpl(NDArray<?> rhs) {
      if (rhs instanceof DenseFloat32NDArray) {
         DenseFloat32NDArray n = Cast.as(rhs);
         return new DenseFloat32NDArray(matrices[0].mmul(n.matrices[0]));
      }
      return super.matrixMultiplicationImpl(rhs);
   }

   @Override
   public NDArray<Float> reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException("Cannot change total length from " +
                                                  shape().length() +
                                                  " to " +
                                                  newShape.length());
      }
      FloatMatrix[] temp = new FloatMatrix[newShape.sliceLength()];
      for (int i = 0; i < temp.length; i++) {
         temp[i] = FloatMatrix.zeros(newShape.rows(), newShape.columns());
      }
      for (int i = 0; i < length(); i++) {
         double v = getDouble(i);
         int sliceIndex = newShape.toSliceIndex(i);
         int matrixIndex = newShape.toMatrixIndex(i);
         temp[sliceIndex].put(matrixIndex, (float) v);
      }
      this.matrices = temp;
      shape().reshape(newShape);
      return this;
   }



   @Override
   public NDArray<Float> set(int kernel, int channel, int row, int col, @NonNull Float value) {
      matrices[shape().calculateSliceIndex(kernel, channel)].put(row, col, value);
      return this;
   }


   @Override
   public NDArray<Float> set(int kernel, int channel, int row, int col, double value) {
      matrices[shape().calculateSliceIndex(kernel, channel)].put(row, col, (float) value);
      return this;
   }

   @Override
   public NDArray<Float> setAxis(int axis, int position, @NonNull NDArray<Float> rhs) {
      return super.setAxisDouble(axis, position, rhs);
   }

   @Override
   public NDArray<Float> setAxis(int axis, int position, @NonNull Float rhs) {
      return super.setAxisDouble(axis, position, rhs);
   }

   @Override
   public NDArray<Float> setAxisDouble(int axis, int position, @NonNull NDArray<?> rhs) {
      if (rhs instanceof DenseFloat32NDArray) {
         DenseFloat32NDArray df = Cast.as(rhs);
         int absAxis = shape().toAbsolute(axis);
         switch (absAxis) {
            case 3:
               return forEachMatrix(df, (a, b) -> a.putColumn(position, b));
            case 2:
               return forEachMatrix(df, (a, b) -> a.putRow(position, b));
         }
      }
      return super.setAxisDouble(axis, position, rhs);
   }

   @Override
   public NDArray<Float> setSlice(int index, @NonNull NDArray<Float> slice) {
      if (!slice.shape().equals(shape().matrixShape())) {
         throw new IllegalArgumentException("Unable to set slice of different shape");
      }
      if (slice instanceof DenseFloat32NDArray) {
         DenseFloat32NDArray m = Cast.as(slice);
         matrices[index].copy(m.matrices[0]);
      } else {
         for (Index ii : shape().matrixShape().range()) {
            matrices[index].put(ii.getRow(), ii.getColumn(), (float) slice.getDouble(ii));
         }
      }
      return this;
   }

   @Override
   public NDArray<Float> slice(int index) {
      return new DenseFloat32NDArray(matrices[index]);
   }

   @Override
   public NDArray<Float> slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      DenseFloat32NDArray v = new DenseFloat32NDArray(os);
      for (int kernel = startKernel; kernel < endKernel; kernel++) {
         for (int channel = startChannel; channel < endChannel; channel++) {
            int ti = shape().calculateSliceIndex(kernel, channel);
            int oi = os.calculateSliceIndex(kernel - startKernel, channel - startChannel);
            v.matrices[oi] = matrices[ti];
         }
      }
      return v;
   }


   @Override
   protected Object arrayForTensor() {
      if (shape().isEmpty() || shape().isVector()) {
         return matrices[0].toArray();
      } else if (shape().isMatrix()) {
         return matrices[0].toArray2();
      }
      return super.arrayForTensor();
   }

   @Override
   public DoubleMatrix[] toDoubleMatrix() {
      DoubleMatrix[] m = new DoubleMatrix[matrices.length];
      for (int i = 0; i < matrices.length; i++) {
         m[i] = MatrixFunctions.floatToDouble(matrices[i]);
      }
      return m;
   }

   @Override
   public FloatMatrix[] toFloatMatrix() {
      return matrices;
   }


   @FunctionalInterface
   protected interface BiMatrixConsumer {

      void accept(FloatMatrix a, FloatMatrix b);
   }

   @FunctionalInterface
   protected interface MatrixDoubleConsumer {

      void accept(FloatMatrix a, double b);
   }

}//END OF DenseFloat32NDArray
