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

import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.util.function.Supplier;

/**
 * <p>Base factory for creating ObjectNDArray.</p>
 *
 * @param <T> the type parameter
 */
public abstract class ObjectNDArrayFactory<T> extends NDArrayFactory {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new ObjectNDArrayFactory.
    *
    * @param c the class information for the object
    */
   public ObjectNDArrayFactory(Class<?> c) {
      super(c);
   }


   /**
    * <p>Constructs an NDArray with given <code>shape</code> setting its values to those in the supplied array.</p>
    *
    * @param shape the shape
    * @param a     the values to initialize the NDArray
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> array(@NonNull Shape shape, T[] a) {
      ObjectNDArray<T> n = zeros(shape);
      for (int ni = 0; ni < a.length; ni++) {
         n.set(ni, a[ni]);
      }
      return n;
   }


   /**
    * <p>Constructs a vector setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> array(@NonNull T[] a) {
      ObjectNDArray<T> n = zeros(a.length);
      for (int ni = 0; ni < a.length; ni++) {
         n.set(ni, a[ni]);
      }
      return n;
   }

   /**
    * <p>Constructs a matrix setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> array(@NonNull T[][] a) {
      ObjectNDArray<T> n = zeros(a.length, a[0].length);
      for (int row = 0; row < a.length; row++) {
         for (int column = 0; column < a[row].length; column++) {
            n.set(row, column, a[row][column]);
         }
      }
      return n;
   }

   /**
    * <p>Constructs a tensor setting its values to those in the supplied array.</p>
    *
    * @param a the values to initialize the NDArray
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> array(T[][][] a) {
      ObjectNDArray<T> n = zeros(a.length, a[0].length, a[0][0].length);
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
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> array(T[][][][] a) {
      ObjectNDArray<T> n = zeros(a.length, a[0].length, a[0][0].length, a[0][0][0].length);
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
    * <p>Creates a new vector with given <code>length</code> initializing its values using the given
    * <code>initializer</code></p>
    *
    * @param length      the length of the vector
    * @param initializer the initializer
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(int length, @NonNull Supplier<? extends T> initializer) {
      return create(Shape.shape(length), initializer);
   }

   /**
    * <p>Creates a new Matrix with given number of <code>rows</code> and <code>columns</code> initializing its values
    * using the given <code>initializer</code></p>
    *
    * @param rows        the number of rows in the matrix
    * @param columns     the number of columns in the matrix
    * @param initializer the initializer
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(int rows, int columns, @NonNull Supplier<? extends T> initializer) {
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
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(int channels, int rows, int columns, @NonNull Supplier<? extends T> initializer) {
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
    */
   public ObjectNDArray<T> create(int kernels, int channels, int rows, int columns, @NonNull Supplier<? extends T> initializer) {
      return create(Shape.shape(kernels, channels, rows, columns), initializer);
   }

   /**
    * <p>Creates a new NDArray with given <code>shape</code> initializing its values using the given
    * <code>generator</code></p>
    *
    * @param shape     the shape
    * @param generator the supplier that generates new values for the NDArray
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(@NonNull Shape shape, @NonNull Supplier<? extends T> generator) {
      ObjectNDArray<T> out = zeros(shape);
      for (Index index : shape.range()) {
         out.set(index, generator.get());
      }
      return out;
   }

   /**
    * <p>Creates a new NDArray with given <code>shape</code> initializing its values using the given
    * <code>initializer</code></p>
    *
    * @param shape       the shape
    * @param initializer the initializer
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(@NonNull Shape shape, @NonNull NDArrayInitializer<? extends ObjectNDArray<T>> initializer) {
      ObjectNDArray<T> zeros = zeros(shape);
      initializer.accept(Cast.as(zeros));
      return zeros;
   }

   /**
    * <p>Creates a new vector with given <code>length</code> initializing its values using the given
    * <code>initializer</code></p>
    *
    * @param length      the length of the vector
    * @param initializer the initializer
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(int length, @NonNull NDArrayInitializer<? extends ObjectNDArray<T>> initializer) {
      return create(Shape.shape(length), initializer);
   }

   /**
    * <p>Creates a new Matrix with given number of <code>rows</code> and <code>columns</code> initializing its values
    * using the given <code>initializer</code></p>
    *
    * @param rows        the number of rows in the matrix
    * @param columns     the number of columns in the matrix
    * @param initializer the initializer
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(int rows, int columns, @NonNull NDArrayInitializer<? extends ObjectNDArray<T>> initializer) {
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
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(int channels, int rows, int columns, @NonNull NDArrayInitializer<? extends ObjectNDArray<T>> initializer) {
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
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> create(int kernels, int channels, int rows, int columns, @NonNull NDArrayInitializer<? extends ObjectNDArray<T>> initializer) {
      return create(Shape.shape(kernels, channels, rows, columns), initializer);
   }

   @Override
   public ObjectNDArray<T> empty() {
      return Cast.as(super.empty());
   }

   /**
    * <p>Creates a scalar NDArray with the given <code>value</code></p>
    *
    * @param value the value
    * @return the ObjectNDArray
    */
   public ObjectNDArray<T> scalar(T value) {
      return zeros(1).fill(value);
   }

   @Override
   public ObjectNDArray<T> zeros(@NonNull int... dims) {
      return zeros(Shape.shape(dims));
   }

   @Override
   public abstract ObjectNDArray<T> zeros(@NonNull Shape shape);
}//END OF ObjectNDArray
