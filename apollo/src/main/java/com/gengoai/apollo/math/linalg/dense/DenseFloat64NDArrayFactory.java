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

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.NumericNDArrayFactory;
import com.gengoai.apollo.math.linalg.Shape;
import lombok.NonNull;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import static com.gengoai.Validation.checkArgument;

/**
 * <p>Factory for creating Dense 64-bit flot NDArrays</p>
 *
 * @author David B. Bracewell
 */
public final class DenseFloat64NDArrayFactory extends NumericNDArrayFactory {
   /**
    * The singleton instance of the Factory
    */
   public static final DenseFloat64NDArrayFactory INSTANCE = new DenseFloat64NDArrayFactory();
   private static final long serialVersionUID = 1L;

   private DenseFloat64NDArrayFactory() {
      super(Double.class);
   }

   /**
    * <p>Creates an NDArray containing the given JBlas FloatMatrix.</p>
    *
    * @param floatMatrix the float matrix
    * @return the NumericNDArray
    */
   public NumericNDArray array(@NonNull FloatMatrix floatMatrix) {
      return new DenseFloat64NDArray(MatrixFunctions.floatToDouble(floatMatrix));
   }

   /**
    * <p>Creates an NDArray containing the given JBlas DoubleMatrix.</p>
    *
    * @param doubleMatrix the double matrix
    * @return the NumericNDArray
    */
   public NumericNDArray array(@NonNull DoubleMatrix doubleMatrix) {
      return new DenseFloat64NDArray(doubleMatrix);
   }

   @Override
   public NumericNDArray array(@NonNull Shape shape, double[] a) {
      if (shape.length() != a.length) {
         throw new IllegalArgumentException("Length mismatch " + a.length + " != " + shape.length());
      } else if (shape.isEmpty()) {
         return empty();
      }
      return new DenseFloat64NDArray(shape, a);
   }

   @Override
   public NumericNDArray array(double[][] a) {
      if (a.length == 0 || a[0].length == 0) {
         return empty();
      }
      return new DenseFloat64NDArray(a);
   }

   @Override
   public NumericNDArray array(double[][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0) {
         return empty();
      }
      return new DenseFloat64NDArray(a);
   }

   @Override
   public NumericNDArray array(double[][][][] a) {
      if (a.length == 0 || a[0].length == 0 || a[0][0].length == 0 || a[0][0][0].length == 0) {
         return empty();
      }
      return new DenseFloat64NDArray(a);
   }

   @Override
   public NumericNDArray empty() {
      return new DenseFloat64NDArray(DoubleMatrix.EMPTY);
   }

   @Override
   public NumericNDArray scalar(@NonNull Number value) {
      return new DenseFloat64NDArray(DoubleMatrix.scalar(value.doubleValue()));
   }

   /**
    * <p>Creates a new Tensor from with given number of <code>kernels</code> and <code>channels</code> where each slice
    * is defined as one the supplied JBlas DoubleMatrix</p>
    *
    * @param kernels  the number of kernels
    * @param channels the number of channels
    * @param matrices the matrices
    * @return the NumericNDArray
    */
   public NumericNDArray tensor(int kernels, int channels, @NonNull DoubleMatrix[] matrices) {
      checkArgument((kernels * channels) == matrices.length,
                    () -> "Length mismatch: expecting " + (kernels * channels) + " matrices, but received " + matrices.length);
      return new DenseFloat64NDArray(kernels, channels, matrices);
   }

   @Override
   public NumericNDArray zeros(@NonNull Shape shape) {
      return new DenseFloat64NDArray(shape);
   }

}//END OF DenseFloat64NDArrayFactory
