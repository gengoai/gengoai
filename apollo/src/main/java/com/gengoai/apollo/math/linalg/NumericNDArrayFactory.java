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

package com.gengoai.apollo.math.linalg;

import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

/**
 * <p>Base factory for creating NumericNDArrays.</p>
 *
 * @author David B. Bracewell
 */
public abstract class NumericNDArrayFactory extends NDArrayFactory {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new NumericNDArrayFactory.
    *
    * @param c the c
    */
   public NumericNDArrayFactory(Class<?> c) {
      super(c);
   }

   /**
    * <p>Constructs a vector of length <code>end-start</code> whose values range from <code>[start, end)</code> at
    * increments of <code>1</code>.</p>
    *
    * @param start the starting value in the range
    * @param end   the ending value in the range
    * @return NumericNDArray
    */
   public NumericNDArray arange(double start, double end) {
      return arange(start, end, 1);
   }

   /**
    * <p>Constructs a vector  whose values range from <code>[start, end)</code> at increments of
    * <code>increment</code>.</p>
    *
    * @param start     the starting value in the range
    * @param end       the ending value in the range
    * @param increment the amount to increment each element by
    * @return NumericNDArray
    */
   public NumericNDArray arange(double start, double end, double increment) {
      int length = (int) Math.floor((end - start) / increment);
      if ((start + (length * increment)) < end) {
         length++;
      }
      return arange(Shape.shape(length), start, increment);
   }

   /**
    * <p>Constructs an NDArray of the given <code>shape</code> whose values are sequentially set starting at
    * <code>start</code> and incrementing by <code>1</code> at each element.</p>
    *
    * @param shape the shape of the NDArray
    * @param start the starting value in the range
    * @return NumericNDArray
    */
   public NumericNDArray arange(@NonNull Shape shape, double start) {
      return arange(shape, start, 1);
   }

   /**
    * <p>Constructs an NDArray of the given <code>shape</code> whose values are sequentially set starting at
    * <code>0</code> and incrementing by <code>1</code> at each element.</p>
    *
    * @param shape the shape of the NDArray
    * @return NumericNDArray
    */
   public NumericNDArray arange(@NonNull Shape shape) {
      return arange(shape, 0, 1);
   }

   /**
    * <p>Constructs an NDArray of the given <code>shape</code> whose values are sequentially set starting at
    * <code>start</code> and incrementing by <code>increment</code> at each element.</p>
    *
    * @param shape     the shape of the NDArray
    * @param start     the starting value in the range
    * @param increment the amount to increment each element by
    * @return NumericNDArray
    */
   public NumericNDArray arange(@NonNull Shape shape, double start, double increment) {
      NumericNDArray zero = zeros(shape);
      double value = start;
      for (Index index : shape.range()) {
         if (getType() == String.class) {
            zero.set(index, Cast.as(Double.toString(value)));
         } else {
            zero.set(index, value);
         }
         value += increment;
      }
      return zero;
   }


   /**
    * <p>Constructs an NDArray with given <code>shape</code> setting its values to those in the supplied array.</p>
    *
    * @param shape the shape
    * @param a     the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(@NonNull Shape shape, long[] a) {
      Validation.checkArgument(shape.length() == a.length,
                               () -> "Length mismatch " + a.length + " !=" + shape.length());
      NumericNDArray n = zeros(shape);
      for (int i = 0; i < a.length; i++) {
         n.set(i, a[i]);
      }
      return n;
   }


   /**
    * <p>Constructs a vector setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(@NonNull long[] a) {
      NumericNDArray n = zeros(a.length);
      for (int i = 0; i < a.length; i++) {
         n.set(i, a[i]);
      }
      return n;
   }

   /**
    * <p>Constructs a matrix setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(long[][] a) {
      NumericNDArray n = zeros(a.length, a[0].length);
      for (int row = 0; row < a.length; row++) {
         for (int column = 0; column < a[0].length; column++) {
            n.set(row, column, a[row][column]);
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(long[][][] a) {
      NumericNDArray n = zeros(a.length, a[0].length, a[0][0].length);
      for (int channel = 0; channel < a.length; channel++) {
         for (int row = 0; row < a[channel].length; row++) {
            for (int column = 0; column < a[channel][row].length; column++) {
               n.set(channel, row, column, a[channel][row][column]);
            }
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(long[][][][] a) {
      NumericNDArray n = zeros(a.length, a[0].length, a[0][0].length, a[0][0][0].length);
      for (int kernel = 0; kernel < a.length; kernel++) {
         for (int channel = 0; channel < a[kernel].length; channel++) {
            for (int row = 0; row < a[kernel][channel].length; row++) {
               for (int column = 0; column < a[kernel][channel][row].length; column++) {
                  n.set(kernel, channel, row, column, a[kernel][channel][row][column]);
               }
            }
         }
      }
      return n;
   }

   /**
    * <p>Constructs an NDArray with given <code>shape</code> setting its values to those in the supplied array.</p>
    *
    * @param shape the shape
    * @param a     the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(@NonNull Shape shape, int[] a) {
      Validation.checkArgument(shape.length() == a.length,
                               () -> "Length mismatch " + a.length + " !=" + shape.length());
      NumericNDArray n = zeros(shape);
      for (int i = 0; i < a.length; i++) {
         n.set(i, a[i]);
      }
      return n;
   }

   /**
    * <p>Constructs a vector setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(int[] a) {
      NumericNDArray n = zeros(a.length);
      for (int i = 0; i < a.length; i++) {
         n.set(i, a[i]);
      }
      return n;
   }

   /**
    * <p>Constructs a matrix setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(int[][] a) {
      NumericNDArray n = zeros(a.length, a[0].length);
      for (int row = 0; row < a.length; row++) {
         for (int column = 0; column < a[0].length; column++) {
            n.set(row, column, a[row][column]);
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(int[][][] a) {
      NumericNDArray n = zeros(a.length, a[0].length, a[0][0].length);
      for (int channel = 0; channel < a.length; channel++) {
         for (int row = 0; row < a[channel].length; row++) {
            for (int column = 0; column < a[channel][row].length; column++) {
               n.set(channel, row, column, a[channel][row][column]);
            }
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(int[][][][] a) {
      NumericNDArray n = zeros(a.length, a[0].length, a[0][0].length, a[0][0][0].length);
      for (int kernel = 0; kernel < a.length; kernel++) {
         for (int channel = 0; channel < a[kernel].length; channel++) {
            for (int row = 0; row < a[kernel][channel].length; row++) {
               for (int column = 0; column < a[kernel][channel][row].length; column++) {
                  n.set(kernel, channel, row, column, a[kernel][channel][row][column]);
               }
            }
         }
      }
      return n;
   }

   /**
    * <p>Constructs an NDArray with given <code>shape</code> setting its values to those in the supplied array.</p>
    *
    * @param shape the shape
    * @param a     the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(@NonNull Shape shape, float[] a) {
      Validation.checkArgument(shape.length() == a.length,
                               () -> "Length mismatch " + a.length + " !=" + shape.length());
      NumericNDArray n = zeros(shape);
      for (int i = 0; i < a.length; i++) {
         n.set(i, a[i]);
      }
      return n;
   }

   /**
    * <p>Constructs a vector setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(float[] a) {
      NumericNDArray n = zeros(a.length);
      for (int i = 0; i < a.length; i++) {
         n.set(i, a[i]);
      }
      return n;
   }

   /**
    * <p>Constructs a matrix setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(float[][] a) {
      NumericNDArray n = zeros(a.length, a[0].length);
      for (int row = 0; row < a.length; row++) {
         for (int column = 0; column < a[0].length; column++) {
            n.set(row, column, a[row][column]);
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(float[][][] a) {
      NumericNDArray n = zeros(a.length, a[0].length, a[0][0].length);
      for (int channel = 0; channel < a.length; channel++) {
         for (int row = 0; row < a[channel].length; row++) {
            for (int column = 0; column < a[channel][row].length; column++) {
               n.set(channel, row, column, a[channel][row][column]);
            }
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(float[][][][] a) {
      NumericNDArray n = zeros(a.length, a[0].length, a[0][0].length, a[0][0][0].length);
      for (int kernel = 0; kernel < a.length; kernel++) {
         for (int channel = 0; channel < a[kernel].length; channel++) {
            for (int row = 0; row < a[kernel][channel].length; row++) {
               for (int column = 0; column < a[kernel][channel][row].length; column++) {
                  n.set(kernel, channel, row, column, a[kernel][channel][row][column]);
               }
            }
         }
      }
      return n;
   }

   /**
    * <p>Constructs an NDArray with given <code>shape</code> setting its values to those in the supplied array.</p>
    *
    * @param shape the shape
    * @param a     the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(@NonNull Shape shape, double[] a) {
      Validation.checkArgument(shape.length() == a.length,
                               () -> "Length mismatch " + a.length + " !=" + shape.length());
      NumericNDArray n = zeros(shape);
      for (int i = 0; i < a.length; i++) {
         n.set(i, a[i]);
      }
      return n;
   }

   /**
    * <p>Constructs a vector setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(double[] a) {
      NumericNDArray n = zeros(a.length);
      for (int i = 0; i < a.length; i++) {
         n.set(i, a[i]);
      }
      return n;
   }

   /**
    * <p>Constructs a matrix setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(double[][] a) {
      NumericNDArray n = zeros(a.length, a[0].length);
      for (int row = 0; row < a.length; row++) {
         for (int column = 0; column < a[0].length; column++) {
            n.set(row, column, a[row][column]);
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(double[][][] a) {
      NumericNDArray n = zeros(a.length, a[0].length, a[0][0].length);
      for (int channel = 0; channel < a.length; channel++) {
         for (int row = 0; row < a[channel].length; row++) {
            for (int column = 0; column < a[channel][row].length; column++) {
               n.set(channel, row, column, a[channel][row][column]);
            }
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the NumericNDArray
    */
   public NumericNDArray array(double[][][][] a) {
      NumericNDArray n = zeros(a.length, a[0].length, a[0][0].length, a[0][0][0].length);
      for (int kernel = 0; kernel < a.length; kernel++) {
         for (int channel = 0; channel < a[kernel].length; channel++) {
            for (int row = 0; row < a[kernel][channel].length; row++) {
               for (int column = 0; column < a[kernel][channel][row].length; column++) {
                  n.set(kernel, channel, row, column, a[kernel][channel][row][column]);
               }
            }
         }
      }
      return n;
   }


   @Override
   public NumericNDArrayFactory asNumeric() {
      return this;
   }

   /**
    * <p>Creates a new NDArray with given <code>shape</code> initializing its values using the given
    * <code>initializer</code></p>
    *
    * @param shape       the shape
    * @param initializer the initializer
    * @return the NumericNDArray
    */
   public NumericNDArray create(@NonNull Shape shape, @NonNull NDArrayInitializer<NumericNDArray> initializer) {
      NumericNDArray zeros = zeros(shape);
      initializer.accept(Cast.as(zeros));
      return zeros;
   }

   /**
    * <p>Creates a new vector with given <code>length</code> initializing its values using the given
    * <code>initializer</code></p>
    *
    * @param length      the length of the vector
    * @param initializer the initializer
    * @return the NumericNDArray
    */
   public NumericNDArray create(int length, @NonNull NDArrayInitializer<NumericNDArray> initializer) {
      return create(Shape.shape(length), initializer);
   }

   /**
    * <p>Creates a new Matrix with given number of <code>rows</code> and <code>columns</code> initializing its values
    * using the given <code>initializer</code></p>
    *
    * @param rows        the number of rows in the matrix
    * @param columns     the number of columns in the matrix
    * @param initializer the initializer
    * @return the NumericNDArray
    */
   public NumericNDArray create(int rows, int columns, @NonNull NDArrayInitializer<NumericNDArray> initializer) {
      return create(Shape.shape(rows, columns), initializer);
   }

   /**
    * <p>Creates a new 3d-tensor with given number of <code>channels</code>, <code>rows</code> and <code>columns</code>
    * initializing its values using the given <code>initializer</code></p>
    *
    * @param channels    the number of channels in the matrix
    * @param rows        the number of rows in the matrix
    * @param columns     the number of columns in the matrix
    * @param initializer the initializer
    * @return the NumericNDArray
    */
   public NumericNDArray create(int channels, int rows, int columns, @NonNull NDArrayInitializer<NumericNDArray> initializer) {
      return create(Shape.shape(channels, rows, columns), initializer);
   }

   /**
    * <p>Creates a new 3d-tensor with given number of <code>kernels</code>, <code>channels</code>, <code>rows</code>
    * and <code>columns</code> initializing its values using the given <code>initializer</code></p>
    *
    * @param kernels     the number of kernels in the matrix
    * @param channels    the number of channels in the matrix
    * @param rows        the number of rows in the matrix
    * @param columns     the number of columns in the matrix
    * @param initializer the initializer
    * @return the NumericNDArray
    */
   public NumericNDArray create(int kernels, int channels, int rows, int columns, @NonNull NDArrayInitializer<NumericNDArray> initializer) {
      return create(Shape.shape(kernels, channels, rows, columns), initializer);
   }

   @Override
   public NumericNDArray empty() {
      return Cast.as(super.empty());
   }


   /**
    * <p>Creates a new NDArray with given <code>shape</code> initializing its values to all <code>1</code></p>
    *
    * @param shape the shape
    * @return the NumericNDArray
    */
   public NumericNDArray ones(@NonNull Shape shape) {
      return zeros(shape).fill(1);
   }

   /**
    * <p>Creates a new NDArray with given <code>dimensions</code> initializing its values to all <code>1</code></p>
    *
    * @param dimensions the dimensions
    * @return the NumericNDArray
    */
   public NumericNDArray ones(@NonNull int... dimensions) {
      return ones(Shape.shape(dimensions));
   }


   /**
    * <p>Creates a scalar NDArray with the given <code>value</code></p>
    *
    * @param value the value
    * @return the NumericNDArray
    */
   public abstract NumericNDArray scalar(@NonNull Number value);

   /**
    * <p>Creates a scalar NDArray with the given <code>value</code></p>
    *
    * @param value the value
    * @return the NumericNDArray
    */
   public NumericNDArray scalar(double value) {
      return zeros(Shape.shape(1)).set(0, value);
   }

   @Override
   public NumericNDArray zeros(@NonNull int... dims) {
      return zeros(Shape.shape(dims));
   }

   @Override
   public abstract NumericNDArray zeros(@NonNull Shape shape);
}//END OF NumericNDArrayFactory
