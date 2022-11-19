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
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;
import org.tensorflow.Tensor;
import org.tensorflow.proto.framework.DataType;
import org.tensorflow.types.TFloat64;

import java.util.function.UnaryOperator;

import static com.gengoai.Validation.checkArgument;

/**
 * <p>Dense NDArray representing 64-bit float values.</p>
 *
 * @author David B. Bracewell
 */
public final class DenseFloat64NDArray extends DoubleNDArray {
   private static final long serialVersionUID = 1L;
   private DoubleMatrix[] matrices;

   protected DenseFloat64NDArray(@NonNull Shape shape,
                                 @NonNull double... data) {
      this(shape);
      for (int i = 0; i < data.length; i++) {
         set(i, data[i]);
      }
   }

   @JsonCreator
   protected DenseFloat64NDArray(@JsonProperty("data") double[] data,
                                 @JsonProperty("shape") Shape shape,
                                 @JsonProperty("label") Object label,
                                 @JsonProperty("predicted") Object predicted,
                                 @JsonProperty("weight") double weight) {
      this(shape, data);
      setLabel(label);
      setPredicted(predicted);
      setWeight(weight);
   }


   protected DenseFloat64NDArray(@NonNull double... data) {
      super(Shape.shape(data.length));
      this.matrices = new DoubleMatrix[]{new DoubleMatrix(1, data.length, data)};
   }

   protected DenseFloat64NDArray(@NonNull double[][] data) {
      super(Shape.shape(data.length, data[0].length));
      this.matrices = new DoubleMatrix[]{new DoubleMatrix(data)};
   }

   protected DenseFloat64NDArray(@NonNull double[][][] data) {
      super(Shape.shape(data.length, data[0].length, data[0][0].length));
      this.matrices = new DoubleMatrix[shape().sliceLength()];
      for (int channel = 0; channel < data.length; channel++) {
         this.matrices[channel] = new DoubleMatrix(data[channel]);
      }
   }

   protected DenseFloat64NDArray(@NonNull double[][][][] data) {
      super(Shape.shape(data.length, data[0].length, data[0][0].length, data[0][0][0].length));
      this.matrices = new DoubleMatrix[shape().sliceLength()];
      for (int kernel = 0; kernel < data.length; kernel++) {
         for (int channel = 0; channel < data[kernel].length; channel++) {
            this.matrices[shape().calculateSliceIndex(kernel, channel)] = new DoubleMatrix(data[kernel][channel]);
         }
      }
   }


   protected DenseFloat64NDArray(@NonNull Shape shape) {
      super(shape);
      this.matrices = new DoubleMatrix[shape.sliceLength()];
      for (int i = 0; i < this.matrices.length; i++) {
         this.matrices[i] = new DoubleMatrix(Math.max(1, shape.rows()), shape.columns());
      }
   }

   protected DenseFloat64NDArray(@NonNull DoubleMatrix fm) {
      super(Shape.shape(fm.rows, fm.columns));
      this.matrices = new DoubleMatrix[]{fm};
   }


   private DenseFloat64NDArray(@NonNull Shape shape, @NonNull DoubleMatrix[] fm) {
      super(shape);
      this.matrices = fm;
   }

   protected DenseFloat64NDArray(int kernels, int channels, @NonNull DoubleMatrix[] fm) {
      this(Shape.shape(kernels, channels, fm[0].rows, fm[0].columns), fm);
   }

   /**
    * <p>Converts TensorFlow Tenors for Float type to DenseFloat32NDArray.</p>
    *
    * @param tensor the tensor
    * @return the converted Tensor
    */
   public static NumericNDArray fromTensor(@NonNull Tensor tensor) {
      if (tensor.dataType() == DataType.DT_DOUBLE) {
         TFloat64 ndarray = Cast.as(tensor);
         NumericNDArray rval = nd.DFLOAT64.zeros(Shape.shape(tensor.shape().asArray()));
         ndarray.scalars().forEachIndexed((coords, value) -> rval.set(coords, value.getDouble()));
      }
      throw new IllegalArgumentException("Unsupported type '" + tensor.dataType().name() + "'");
   }

   @Override
   public NumericNDArray T() {
      if (shape().isScalar() || shape().isEmpty()) {
         return copy();
      }
      return mapSlices(DoubleMatrix::transpose);
   }

   @Override
   public NumericNDArray fill(double value) {
      return forEachMatrix(value, (a, b) -> a.fill((float) value));
   }

   @Override
   public Double get(int kernel, int channel, int row, int col) {
      return matrices[shape().calculateSliceIndex(kernel, channel)].get(row, col);
   }

   @Override
   public double getDouble(int kernel, int channel, int row, int col) {
      return matrices[shape().calculateSliceIndex(kernel, channel)].get(row, col);
   }

   @Override
   public Class<?> getType() {
      return Double.class;
   }

   @Override
   public boolean isDense() {
      return true;
   }

   @Override
   public NumericNDArray reshape(@NonNull Shape newShape) {
      if (shape().length() != newShape.length()) {
         throw new IllegalArgumentException("Cannot change total length from " +
                                            shape().length() +
                                            " to " +
                                            newShape.length());
      }
      DoubleMatrix[] temp = new DoubleMatrix[newShape.sliceLength()];
      for (int i = 0; i < temp.length; i++) {
         temp[i] = DoubleMatrix.zeros(newShape.rows(), newShape.columns());
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
   public NumericNDArray set(int kernel, int channel, int row, int col, @NonNull Object value) {
      checkArgument(value instanceof Number, () -> "Cannot fill NumericNDArray with '" + value.getClass()
                                                                                              .getSimpleName() + "' value.");
      matrices[shape().calculateSliceIndex(kernel, channel)].put(row, col, Cast.as(value, Number.class).floatValue());
      return this;
   }

   @Override
   public NumericNDArray set(int kernel, int channel, int row, int col, double value) {
      matrices[shape().calculateSliceIndex(kernel, channel)].put(row, col, (float) value);
      return this;
   }

   @Override
   public NumericNDArray setAxisDouble(int axis, int position, @NonNull NumericNDArray rhs) {
      if (rhs instanceof DenseFloat64NDArray) {
         DenseFloat64NDArray df = Cast.as(rhs);
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
   public NumericNDArray setSlice(int index, @NonNull NDArray slice) {
      if (!slice.shape().equals(shape().matrixShape())) {
         throw new IllegalArgumentException("Unable to set slice of different shape");
      }
      if (slice instanceof DenseFloat64NDArray) {
         DenseFloat64NDArray m = Cast.as(slice);
         matrices[index].copy(m.matrices[0]);
      }
      return super.setSlice(index, slice);
   }

   @Override
   public NumericNDArray slice(int index) {
      if (matrices.length == 1) {
         return new DenseFloat64NDArray(matrices[0]);
      }
      return new DenseFloat64NDArray(matrices[index]);
   }

   @Override
   public NumericNDArray slice(int startKernel, int startChannel, int endKernel, int endChannel) {
      Shape os = toSliceShape(startKernel, startChannel, endKernel, endChannel);
      DenseFloat64NDArray v = new DenseFloat64NDArray(os);
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
   @JsonProperty("data")
   public double[] toDoubleArray() {
      return super.toDoubleArray();
   }

   @Override
   public DoubleMatrix[] toDoubleMatrix() {
      return matrices;
   }

   @Override
   public FloatMatrix[] toFloatMatrix() {
      FloatMatrix[] m = new FloatMatrix[matrices.length];
      for (int i = 0; i < matrices.length; i++) {
         m[i] = MatrixFunctions.doubleToFloat(matrices[i]);
      }
      return m;
   }

   private NumericNDArray forEachMatrix(DenseFloat64NDArray rhs, BiMatrixConsumer op) {
      for (Index index : shape().sliceIterator()) {
         int ti = shape().calculateSliceIndex(index);
         int ri = rhs.shape().calculateSliceIndex(rhs.shape().broadcast(index));
         op.accept(matrices[ti], rhs.matrices[ri]);
      }
      return this;
   }

   private NumericNDArray forEachMatrix(double value, MatrixDoubleConsumer op) {
      for (DoubleMatrix matrix : matrices) {
         op.accept(matrix, value);
      }
      return this;
   }

   private NumericNDArray mapSlices(UnaryOperator<DoubleMatrix> op) {
      if (matrices.length == 0) {
         return factory().empty();
      }
      DoubleMatrix[] fm = new DoubleMatrix[matrices.length];
      for (int i = 0; i < matrices.length; i++) {
         fm[i] = op.apply(matrices[i]);
      }
      return new DenseFloat64NDArray(shape().with(Shape.ROW, fm[0].rows,
                                                  Shape.COLUMN, fm[0].columns),
                                     fm);
   }

   @Override
   protected NumericNDArray matrixMultiplicationImpl(NumericNDArray rhs) {
      if (rhs instanceof DenseFloat64NDArray) {
         DenseFloat64NDArray n = Cast.as(rhs);
         return new DenseFloat64NDArray(matrices[0].mmul(n.matrices[0]));
      } else {
         return new DenseFloat64NDArray(matrices[0].mmul(rhs.toDoubleMatrix()[0]));
      }
   }

   @FunctionalInterface
   private interface BiMatrixConsumer {
      void accept(DoubleMatrix a, DoubleMatrix b);
   }

   @FunctionalInterface
   private interface MatrixDoubleConsumer {
      void accept(DoubleMatrix a, double b);
   }
}//END OF DenseFloat64NDArray
