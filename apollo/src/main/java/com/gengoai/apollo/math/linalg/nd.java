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

import com.gengoai.Math2;
import com.gengoai.Validation;
import com.gengoai.apollo.math.NumericComparison;
import com.gengoai.apollo.math.linalg.dense.*;
import com.gengoai.apollo.math.linalg.sparse.*;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.tensorflow.Tensor;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * <p>Convenience methods for manipulating NDArray.</p>
 *
 * @author David B. Bracewell
 */
public final class nd {
   /**
    * Factory for creating a dense NumericNDArray that use ints for storage
    */
   public static final DenseInt32NDArrayFactory DINT32 = DenseInt32NDArrayFactory.INSTANCE;
   /**
    * Factory for creating a dense NumericNDArray that use longs for storage
    */
   public static final DenseInt64NDArrayFactory DINT64 = DenseInt64NDArrayFactory.INSTANCE;
   /**
    * Factory for creating a dense NumericNDArray that use floats for storage
    */
   public static final DenseFloat32NDArrayFactory DFLOAT32 = DenseFloat32NDArrayFactory.INSTANCE;
   /**
    * Factory for creating a dense NumericNDArray that use doubles for storage
    */
   public static final DenseFloat64NDArrayFactory DFLOAT64 = DenseFloat64NDArrayFactory.INSTANCE;
   /**
    * Factory for creating a dense ObjectNDArray with String as the data type
    */
   public static final DenseStringNDArrayFactory DSTRING = DenseStringNDArrayFactory.INSTANCE;
   /**
    * Factory for creating a sparse NumericNDArray that use floats for storage
    */
   public static final SparseFloat32NDArrayFactory SFLOAT32 = SparseFloat32NDArrayFactory.INSTANCE;
   /**
    * Factory for creating a sparse NumericNDArray that use double for storage
    */
   public static final SparseFloat64NDArrayFactory SFLOAT64 = SparseFloat64NDArrayFactory.INSTANCE;
   /**
    * Factory for creating a sparse NumericNDArray that use long for storage
    */
   public static final SparseInt32NDArrayFactory SINT32 = SparseInt32NDArrayFactory.INSTANCE;
   /**
    * Factory for creating a sparse NumericNDArray that use long for storage
    */
   public static final SparseInt64NDArrayFactory SINT64 = SparseInt64NDArrayFactory.INSTANCE;
   /**
    * Factory for creating a sparse ObjectNDArray with String as the data type
    */
   public static final SparseStringNDArrayFactory SSTRING = SparseStringNDArrayFactory.INSTANCE;

   private nd() {
      throw new IllegalAccessError();
   }

   /**
    * <p>Converts the values in the given NumericNDArray to their absolute values returning a new NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math.abs</code> to
    * @return the new NumericNDArray with absolute values
    */
   public static NumericNDArray abs(@NonNull NumericNDArray n) {
      return n.map(Math::abs);
   }

   /**
    * <p>Converts the values in the given NumericNDArray to their absolute values in-place.</p>
    *
    * @param n the NDArray to apply <code>Math.abs</code> to
    * @return the given NumericNDArray
    */
   public static NumericNDArray absi(@NonNull NumericNDArray n) {
      return n.mapi(Math::abs);
   }


   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.ceil</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math.ceil</code> to
    * @return the new NumericNDArray with <code>Math.ceil</code> applied
    */
   public static NumericNDArray ceil(@NonNull NumericNDArray n) {
      return n.map(Math::ceil);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.ceil</code> in-place.</p>
    *
    * @param n the NDArray to apply <code>Math.ceil</code> to
    * @return the given NumericNDArray with <code>Math.ceil</code> applied
    */
   public static NumericNDArray ceili(@NonNull NumericNDArray n) {
      return n.mapi(Math::ceil);
   }

   /**
    * <p>Concatenates the given list of NDArray along the given axis</p>
    *
    * @param <T>    the NDArray type parameter
    * @param axis   the axis to concatenate along
    * @param arrays the arrays to concatenate
    * @return the concatenated arrays
    */
   @SafeVarargs
   public static <T extends NDArray> T concatenate(int axis, @NonNull T... arrays) {
      return concatenate(axis, List.of(arrays));
   }

   /**
    * <p>Concatenates the given list of NDArray along the given axis</p>
    *
    * @param <T>    the NDArray type parameter
    * @param axis   the axis to concatenate along
    * @param arrays the arrays to concatenate
    * @return the concatenated arrays
    */
   public static <T extends NDArray> T concatenate(int axis, @NonNull List<T> arrays) {
      Validation.checkArgument(arrays.size() > 0);
      int absAxis = Shape.shape(1, 1, 1, 1).toAbsolute(axis);
      int newSize = arrays.stream().mapToInt(n -> Math.max(1, n.shape().get(axis))).sum();
      Shape s = arrays.get(0).shape().with(absAxis, newSize);
      NDArray out = arrays.get(0).factory().zeros(s);
      int offset = 0;
      for (NDArray array : arrays) {
         for (Index index : array.shape().range()) {
            Object value = array.get(index);
            index.set(absAxis, index.get(absAxis) + offset);
            out.set(index, value);
         }
         offset += Math.max(1, array.shape().get(absAxis));
      }
      return Cast.as(out);
   }

   /**
    * <p>Constructs an NDArray from a TensorFlow Tensor.</p>
    *
    * @param tensor the TensorFlow Tensor
    * @return the NDArray
    */
   public static NDArray convertTensor(@NonNull Tensor tensor) {
      switch (tensor.dataType()) {
         case DT_FLOAT:
            return Cast.as(DenseFloat32NDArray.fromTensor(Cast.as(tensor)));
         case DT_DOUBLE:
            return Cast.as(DenseFloat64NDArray.fromTensor(Cast.as(tensor)));
         case DT_INT32:
            return Cast.as(DenseInt32NDArray.fromTensor(Cast.as(tensor)));
         case DT_INT64:
            return Cast.as(DenseInt64NDArray.fromTensor(Cast.as(tensor)));
         case DT_STRING:
            return Cast.as(DenseStringNDArray.fromTensor(Cast.as(tensor)));
      }
      throw new IllegalArgumentException("Cannot create NDArray from Tensor of type '" + tensor.dataType() + "'");
   }

   /**
    * <p>Dot-Product between two NumericNDArray.</p>
    * <ul>
    *    <li>If both <code>a</code> and <code>b</code> are empty, the result is an empty NDArray.</li>
    *    <li>If both <<code>b</code> is scalar, a scalar multiplication is performed.</li>
    *    <li>If both <code>a</code> and <code>b</code> are vectors of same length, the inner product is calculated.</li>
    *    <li>f <code>a</code> is a vector and <code>b</code> has the same length and number of rows as <code>a</code>,
    *    the inner product is calculated with <code>b.T()</code> with the result reshaped with 1 row.</li>
    *    <li>f <code>a</code> is a vector and <code>b</code> has the same length and number of columns as <code>a</code>,
    *    the inner product is calculated with <code>b.T()</code> with the result reshaped with 1 column.</li>
    *    <li>If both <code>a</code> and <code>b</code> are matrices, matrix multiplication is performed.</li>
    *    <li>If both <code>a</code> and <code>b</code> are m-NDArrays, matrix multiplication is performed per slice.</li>
    *    <li>If <code>a</code> is an m-NDArray and <code>b</code> a compatible matrix, matrix multiplication is performed per slice.</li>
    *    <li>If <code>a</code> is a matrix and <code>b</code> is a  row-vector,
    *    matrix multiplication is performed when <code>a.columns() == b.row()s</code>, otherwise the inner product is
    *    performed in the ROW direction.</li>
    *    <li>If <code>a</code> is a matrix and <code>b</code> is a  column-vector,
    *    matrix multiplication is performed when <code>a.columns() == b.row()s</code>, otherwise the inner product is
    *    performed in the COLUMN direction.</li>
    *    <li>If <code>a</code> is an m-NDArray and <code>b</code> is a  row-vector,
    *    matrix multiplication is performed per slice when <code>a.columns() == b.row()s</code>, otherwise the inner
    *    product is performed in the ROW direction per slice.</li>
    *    <li>If <code>a</code> is an m-NDArray and <code>b</code> is a  column-vector,
    *    matrix multiplication is performed per slice when <code>a.columns() == b.row()s</code>, otherwise the inner
    *    product is performed per slice in the COLUMN direction.</li>
    * </ul>
    *
    * @param a the first NumericNDArray
    * @param b the second NumericNDArray
    * @return the resultant NumericNDArray
    */
   public static NumericNDArray dot(@NonNull NumericNDArray a, @NonNull NumericNDArray b) {
      var broadCast = a.shape().accepts(b.shape());
      switch (broadCast) {
         case EMPTY:
            return a.copy();
         case SCALAR:
            return a.mul(b.scalarDouble());
         case VECTOR:
            return a.factory().scalar(a.dot(b));

         case MATRIX:
         case TENSOR:
         case TENSOR_CHANNEL:
         case TENSOR_KERNEL:
            return a.mmul(b);

         case MATRIX_ROW:
         case TENSOR_ROW:
            if (a.shape().columns() == b.shape().rows()) {
               return a.mmul(b);
            }
            return a.dot(b, Shape.ROW);

         case MATRIX_COLUMN:
         case TENSOR_COLUMN:
            if (a.shape().columns() == b.shape().rows()) {
               return a.mmul(b);
            }
            return a.dot(b, Shape.COLUMN);

      }

      if (b.shape().isVector() && b.shape().length() == a.shape().rows()) {
         return a.dot(b.T(), Shape.ROW).reshape(a.shape().with(Shape.ROW, 1));
      }
      if (b.shape().isVector() && b.shape().length() == a.shape().columns()) {
         return a.dot(b.T(), Shape.COLUMN).reshape(a.shape().with(Shape.COLUMN, 1));
      }

      if (a.shape().isMatrix() && b.shape().isMatrix() && a.columns() == b.rows()) {
         return a.mmul(b);
      }

      throw new IllegalArgumentException(NDArrayOps.unableToBroadcast(b.shape(), a.shape(), broadCast));
   }

   /**
    * <p>Stack arrays in sequence depth wise (along the CHANNEL axis).</p>
    *
    * @param <T>    the NDArray Type parameter
    * @param arrays the arrays to stack
    * @return the stacked NADArray
    */
   @SafeVarargs
   public static <T extends NDArray> T dstack(@NonNull T... arrays) {
      return dstack(List.of(arrays));
   }

   /**
    * <p>Stack arrays in sequence depth wise (along the CHANNEL axis).</p>
    *
    * @param <T>    the NDArray Type parameter
    * @param arrays the arrays to stack
    * @return the stacked NADArray
    */
   public static <T extends NDArray> T dstack(@NonNull List<T> arrays) {
      return concatenate(Shape.CHANNEL, arrays);
   }


   /**
    * <p>Tests if the values in the given <code>lhs</code> are equal to the values in the given <code>rhs</code>
    * NDArray, setting the value to the value in the <code>lhs</code> NDArray when they are equal and to
    * <code>notEqualValue</code> when they are not. Results are stored in a new NDArray</p>
    *
    * @param lhs           the left-hand side NDArray whose values are being tested.
    * @param rhs           the right-hand side NDArray being tested against.
    * @param notEqualValue the value to assign on non-equality
    * @return the NumericNDArray
    */
   public static NumericNDArray eq(@NonNull NumericNDArray lhs,
                                   @NonNull NumericNDArray rhs,
                                   double notEqualValue) {
      Validation.checkArgument(lhs.shape().equals(rhs.shape()), "Shape mismatch " + rhs.shape() + " != " + lhs.shape());
      return lhs.map(rhs, (a, b) -> a == b ? a : notEqualValue);
   }

   /**
    * <p>Tests if the values in the given <code>lhs</code> are equal to the values in the given <code>rhs</code>
    * NDArray, setting the value to <code>equalValue</code> when they are equal and to <code>notEqualValue</code> when
    * they are not. Results are stored in a new NDArray</p>
    *
    * @param lhs           the left-hand side NDArray whose values are being tested.
    * @param rhs           the right-hand side NDArray being tested against.
    * @param equalValue    the value to assign on equality
    * @param notEqualValue the value to assign on non-equality
    * @return the NumericNDArray
    */
   public static NumericNDArray eq(@NonNull NumericNDArray lhs,
                                   @NonNull NumericNDArray rhs,
                                   double equalValue,
                                   double notEqualValue) {
      Validation.checkArgument(lhs.shape().equals(rhs.shape()), "Shape mismatch " + rhs.shape() + " != " + lhs.shape());
      return lhs.map(rhs, (a, b) -> a == b ? equalValue : notEqualValue);
   }

   /**
    * <p>Tests if the values in the given <code>lhs</code> are equal to the values in the given <code>rhs</code>
    * NDArray, setting the value to the value in the <code>lhs</code> NDArray when they are equal and to
    * <code>notEqualValue</code> when they are not. The <code>lhs</code> NDArray is updated in-place.</p>
    *
    * @param lhs           the left-hand side NDArray whose values are being tested.
    * @param rhs           the right-hand side NDArray being tested against.
    * @param notEqualValue the value to assign on non-equality
    * @return the NumericNDArray
    */
   public static NumericNDArray eqi(@NonNull NumericNDArray lhs,
                                    @NonNull NumericNDArray rhs,
                                    double notEqualValue) {
      Validation.checkArgument(lhs.shape().equals(rhs.shape()), "Shape mismatch " + rhs.shape() + " != " + lhs.shape());
      return lhs.mapi(rhs, (a, b) -> a == b ? a : notEqualValue);
   }

   /**
    * <p>Tests if the values in the given <code>lhs</code> are equal to the values in the given <code>rhs</code>
    * NDArray, setting the value to <code>equalValue</code> when they are equal and to <code>notEqualValue</code> when
    * they are not. The <code>lhs</code> NDArray is updated in-place.</p>
    *
    * @param lhs           the left-hand side NDArray whose values are being tested.
    * @param rhs           the right-hand side NDArray being tested against.
    * @param equalValue    the value to assign on equality
    * @param notEqualValue the value to assign on non-equality
    * @return the NumericNDArray
    */
   public static NumericNDArray eqi(@NonNull NumericNDArray lhs,
                                    @NonNull NumericNDArray rhs,
                                    double equalValue,
                                    double notEqualValue) {
      Validation.checkArgument(lhs.shape().equals(rhs.shape()), "Shape mismatch " + rhs.shape() + " != " + lhs.shape());
      return lhs.mapi(rhs, (a, b) -> a == b ? equalValue : notEqualValue);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.exp</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math.exp</code> to
    * @return the new NumericNDArray with <code>Math.exp</code> applied
    */
   public static NumericNDArray exp(@NonNull NumericNDArray n) {
      return n.map(Math::exp);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.exp</code> in-place.</p>
    *
    * @param n the NDArray to apply <code>Math.exp</code> to
    * @return the given NumericNDArray with <code>Math.exp</code> applied
    */
   public static NumericNDArray expi(@NonNull NumericNDArray n) {
      return n.mapi(Math::exp);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.floor</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math.floor</code> to
    * @return the new NumericNDArray with <code>Math.floor</code> applied
    */
   public static NumericNDArray floor(@NonNull NumericNDArray n) {
      return n.map(Math::floor);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.floor</code> in-place.</p>
    *
    * @param n the NDArray to apply <code>Math.floor</code> to
    * @return the given NumericNDArray with <code>Math.floor</code> applied
    */
   public static NumericNDArray floori(@NonNull NumericNDArray n) {
      return n.mapi(Math::floor);
   }

   /**
    * <p>Stack arrays in sequence horizontally (column wise).</p>
    *
    * @param <T>    the NDArray Type parameter
    * @param arrays the arrays to stack
    * @return the stacked NADArray
    */
   @SafeVarargs
   public static <T extends NDArray> T hstack(@NonNull T... arrays) {
      return hstack(List.of(arrays));
   }

   /**
    * <p>Stack arrays in sequence horizontally (column wise).</p>
    *
    * @param <T>    the NDArray Type parameter
    * @param arrays the arrays to stack
    * @return the stacked NADArray
    */
   public static <T extends NDArray> T hstack(@NonNull List<T> arrays) {
      return concatenate(Shape.COLUMN, arrays);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math2.safeLog</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math2.safeLog</code> to
    * @return the new NumericNDArray with <code>Math2.safeLog</code> applied
    */
   public static NumericNDArray log(@NonNull NumericNDArray n) {
      return n.map(Math2::safeLog);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.log10</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math.log10</code> to
    * @return the new NumericNDArray with <code>Math.log10</code> applied
    */
   public static NumericNDArray log10(@NonNull NumericNDArray n) {
      return n.map(Math::log10);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.log10</code> in-place.</p>
    *
    * @param n the NDArray to apply <code>Math.log10</code> to
    * @return the given NumericNDArray with <code>Math.log10</code> applied
    */
   public static NumericNDArray log10i(@NonNull NumericNDArray n) {
      return n.mapi(Math::log10);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math2.safeLog</code> in-place.</p>
    *
    * @param n the NDArray to apply <code>Math2.safeLog</code> to
    * @return the given NumericNDArray with <code>Math2.safeLog</code> applied
    */
   public static NumericNDArray logi(@NonNull NumericNDArray n) {
      return n.mapi(Math2::safeLog);
   }

   /**
    * <p>Applies the given <code>operator</code> to each element in the NDArray returning a new NDArray with the
    * results.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param in       the NDArray whose values will be mapped
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T map(@NonNull T in, @NonNull UnaryOperator<V> operator) {
      NDArray out = in.zeroLike();
      for (long i = 0; i < in.shape().length(); i++) {
         out.set(i, operator.apply(Cast.as(in.get(i))));
      }
      return Cast.as(out);
   }

   /**
    * <p>Applies the given <code>operator</code> to each element in the NDArray returning a new NDArray with the
    * results.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param in       the NDArray whose values will be mapped
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T map(@NonNull T in, V value, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(in, value, operator, null);
   }

   /**
    * <p>Applies the given binary <code>operator</code> to the elements in the <code>lhs</code> and <code>rhs</code>
    * NDArrays, broadcasting where necessary. The result is stored in a new NDArray.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param lhs      the left-hand side NDArray
    * @param rhs      the right-hand side NDArray
    * @param operator the operator to apply
    * @return a new NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T map(@NonNull T lhs, @NonNull T rhs, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(lhs, rhs, operator, null);
   }

   /**
    * <p>Applies the given binary <code>operator</code> to the elements along the given axis in the <code>lhs</code> to
    * those in the <code>rhs</code> NDArrays, broadcasting where necessary. The result is stored in a new NDArray.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param axis     the axis of the <code>lhs</code> NDArray to map
    * @param lhs      the left-hand side NDArray
    * @param rhs      the right-hand side NDArray
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T map(@NonNull T lhs, int axis, @NonNull T rhs, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(lhs, axis, rhs, operator, null);
   }

   /**
    * <p>Applies the given binary <code>operator</code> to the elements along the given axis in the <code>lhs</code> at
    * the given <code>position</code> to those in the <code>rhs</code> NDArrays, broadcasting where necessary. A new
    * NDArray is crated to store the results.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param axis     the axis of the <code>lhs</code> NDArray to map
    * @param position the position along the axis to map
    * @param lhs      the left-hand side NDArray
    * @param rhs      the right-hand side NDArray
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T map(@NonNull T lhs, int axis, int position, @NonNull T rhs, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(lhs, axis, position, rhs, operator, null);
   }

   /**
    * <p>Applies the given binary <code>operator</code> to the elements along the given axis in the <code>lhs</code> at
    * the given <code>position</code> to the given <code>value</code>. A new NDArray is created to store the
    * result.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param axis     the axis of the <code>lhs</code> NDArray to map
    * @param position the position along the axis to map
    * @param lhs      the left-hand side NDArray
    * @param value    the right-hand side value to use in the operation
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T map(@NonNull T lhs, int axis, int position, @NonNull V value, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(lhs, axis, position, value, operator, null);
   }

   /**
    * <p>Applies the given <code>operator</code> to each element in the NDArray returning a new NDArray with the
    * results.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param in       the NDArray whose values will be mapped
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T mapi(@NonNull T in, V value, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(in, value, operator, in);
   }

   /**
    * <p>Applies the given binary <code>operator</code> to the elements along the given axis in the <code>lhs</code> to
    * those in the <code>rhs</code> NDArrays, broadcasting where necessary. The <code>lhs</code> NDArray's elements are
    * updated in place.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param axis     the axis of the <code>lhs</code> NDArray to map
    * @param lhs      the left-hand side NDArray
    * @param rhs      the right-hand side NDArray
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T mapi(@NonNull T lhs, int axis, @NonNull T rhs, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(lhs, axis, rhs, operator, lhs);
   }

   /**
    * <p>Applies the given binary <code>operator</code> to the elements along the given axis in the <code>lhs</code> at
    * the given <code>position</code> to those in the <code>rhs</code> NDArrays, broadcasting where necessary. The
    * <code>lhs</code> NDArray's elements are updated in place.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param axis     the axis of the <code>lhs</code> NDArray to map
    * @param position the position along the axis to map
    * @param lhs      the left-hand side NDArray
    * @param rhs      the right-hand side NDArray
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T mapi(@NonNull T lhs, int axis, int position, @NonNull T rhs, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(lhs, axis, position, rhs, operator, lhs);
   }

   /**
    * <p>Applies the given binary <code>operator</code> to the elements along the given axis in the <code>lhs</code> at
    * the given <code>position</code> to the given <code>value</code>. The <code>lhs</code> NDArray's elements are
    * updated in place.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param axis     the axis of the <code>lhs</code> NDArray to map
    * @param position the position along the axis to map
    * @param lhs      the left-hand side NDArray
    * @param value    the right-hand side value to use in the operation
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T mapi(@NonNull T lhs, int axis, int position, @NonNull V value, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(lhs, axis, position, value, operator, lhs);
   }

   /**
    * <p>Applies the given binary <code>operator</code> to the elements in the <code>lhs</code> and <code>rhs</code>
    * NDArrays, broadcasting where necessary. The results are stored in the <code>lhs</code> NDArray.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param lhs      the left-hand side NDArray
    * @param rhs      the right-hand side NDArray
    * @param operator the operator to apply
    * @return the NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T mapi(@NonNull T lhs, @NonNull T rhs, @NonNull BinaryOperator<V> operator) {
      return NDArrayOps.map(lhs, rhs, operator, lhs);
   }

   /**
    * <p>Applies the given <code>operator</code> to each element in the NDArray in-place.</p>
    *
    * @param <T>      the type of NDArray
    * @param <V>      the data type of the NDArray
    * @param in       the NDArray whose values will be mapped
    * @param operator the operator to apply
    * @return the given <codE>in</codE> NDArray with the results of the operator applied
    * @throws ClassCastException If the given UnaryOperator is for a data type not assignable from the data in the given
    *                            NDArray.
    */
   public static <T extends NDArray, V> T mapi(@NonNull T in, @NonNull UnaryOperator<V> operator) {
      for (long i = 0; i < in.shape().length(); i++) {
         in.set(i, operator.apply(Cast.as(in.get(i))));
      }
      return in;
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.pow</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n     the NDArray to apply <code>Math.pow</code> to
    * @param power the number to raise the NDArray elements to
    * @return the new NumericNDArray with <code>Math.pow</code> applied
    */
   public static NumericNDArray pow(@NonNull NumericNDArray n, int power) {
      return n.map(d -> Math.pow(d, power));
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.pow</code> in-place.</p>
    *
    * @param n     the NDArray to apply <code>Math.pow</code> to
    * @param power the number to raise the NDArray elements to
    * @return the given NumericNDArray with <code>Math.pow</code> applied
    */
   public static NumericNDArray powi(@NonNull NumericNDArray n, int power) {
      return n.mapi(d -> Math.pow(d, power));
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.round</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math.round</code> to
    * @return the new NumericNDArray with <code>Math.round</code> applied
    */
   public static NumericNDArray round(@NonNull NumericNDArray n) {
      return n.map(Math::round);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.round</code> in-place.</p>
    *
    * @param n the NDArray to apply <code>Math.round</code> to
    * @return the given NumericNDArray with <code>Math.round</code> applied
    */
   public static NumericNDArray roundi(@NonNull NumericNDArray n) {
      return n.map(Math::round);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying a sigmoid to the values returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray
    * @return the new NumericNDArray with the sigmoid applied
    */
   public static NumericNDArray sigmoid(@NonNull NumericNDArray n) {
      return n.map(x -> 1.0 / (1.0 + Math.exp(-x)));
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying a sigmoid to the values in-place.</p>
    *
    * @param n the NDArray
    * @return the given NumericNDArray with the sigmoid applied
    */
   public static NumericNDArray sigmoidi(@NonNull NumericNDArray n) {
      return n.map(x -> 1.0 / (1.0 + Math.exp(-x)));
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.signum</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math.signum</code> to
    * @return the new NumericNDArray with <code>Math.signum</code> applied
    */
   public static NumericNDArray signum(@NonNull NumericNDArray n) {
      return n.map(Math::signum);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.signum</code> in-place.</p>
    *
    * @param n the NDArray to apply <code>Math.signum</code> to
    * @return the given NumericNDArray with <code>Math.signum</code> applied
    */
   public static NumericNDArray signumi(@NonNull NumericNDArray n) {
      return n.mapi(Math::signum);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.sqrt</code> returning a new
    * NumericNDArray.</p>
    *
    * @param n the NDArray to apply <code>Math.sqrt</code> to
    * @return the new NumericNDArray with <code>Math.sqrt</code> applied
    */
   public static NumericNDArray sqrt(@NonNull NumericNDArray n) {
      return n.map(Math::sqrt);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by applying <code>Math.sqrt</code> in-place.</p>
    *
    * @param n the NDArray to apply <code>Math.sqrt</code> to
    * @return the given NumericNDArray with <code>Math.sqrt</code> applied
    */
   public static NumericNDArray sqrti(@NonNull NumericNDArray n) {
      return n.mapi(Math::sqrt);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by squaring its values returning a new NumericNDArray.</p>
    *
    * @param n the NDArray
    * @return the given NumericNDArray
    */
   public static NumericNDArray square(@NonNull NumericNDArray n) {
      return n.map(d -> d * d);
   }

   /**
    * <p>Converts the values in the given NumericNDArray by squaring its values in-place.</p>
    *
    * @param n the NDArray
    * @return the given NumericNDArray
    */
   public static NumericNDArray squarei(@NonNull NumericNDArray n) {
      return n.mapi(d -> d * d);
   }

   /**
    * <p>Tests the values in the given NDArray using the given <code>comparison</code> against the given
    * <code>threshold</code>, setting the value to <code>thresholded</code> when the threshold is met and to
    * <code>nonThresholdedValue</code> when not met. Results are stored in a new NDArray</p>
    *
    * @param n                   the NDArray
    * @param comparison          the comparison
    * @param threshold           the threshold
    * @param thresholdedValue    the thresholded value
    * @param nonThresholdedValue the non thresholded value
    * @return the NumericNDArray
    */
   public static NumericNDArray test(@NonNull NumericNDArray n,
                                     @NonNull NumericComparison comparison,
                                     double threshold,
                                     double thresholdedValue,
                                     double nonThresholdedValue) {
      return n.map(d -> comparison.compare(d, threshold) ? thresholdedValue : nonThresholdedValue);
   }

   /**
    * <p>Tests the values in the <code>lhs</code> NDArray against the values in the <code>rhs</code> NDArray using the
    * given <code>comparison</code>, setting the outcome to <code>1</code> when the values are equals and to
    * <code>0</code> when they are not equal. Results are stored in a new NDArray</p>
    *
    * @param lhs        the left-hand side NDArray whose values are being tested.
    * @param rhs        the right-hand side NDArray being tested against.
    * @param comparison the comparison
    * @return the NumericNDArray
    */
   public static NumericNDArray test(@NonNull NumericNDArray lhs,
                                     @NonNull NumericNDArray rhs,
                                     @NonNull NumericComparison comparison) {
      Validation.checkArgument(lhs.shape().equals(rhs.shape()), "Shape mismatch " + rhs.shape() + " != " + lhs.shape());
      return lhs.map(rhs, (a, b) -> comparison.compare(a, b) ? 1 : 0);
   }

   /**
    * <p>Tests the values in the given NDArray using the given <code>comparison</code> against the given
    * <code>compareValue</code>, setting the value to <code>trueVale</code> when the comparison evaluates to TRUE and
    * keeping the current value when it is FALSE. Results are stored in a new NDArray</p>
    *
    * @param n            the NDArray
    * @param comparison   the comparison
    * @param compareValue the value to compare against.
    * @param trueValue    the value to set when the comparison is true.
    * @return the ObjectNDArray
    */
   public static <T> ObjectNDArray<T> test(@NonNull ObjectNDArray<T> n,
                                           @NonNull BiPredicate<T, T> comparison,
                                           T compareValue,
                                           T trueValue) {
      return n.map(d -> comparison.test(d, compareValue) ? trueValue : d);
   }

   /**
    * <p>Tests the values in the given NDArray using the given <code>comparison</code> against the given
    * <code>compareValue</code>, setting the value to <code>trueVale</code> when the comparison evaluates to TRUE and
    * <code>falseValue</code> when it is FALSE. Results are stored in a new NDArray</p>
    *
    * @param n            the NDArray
    * @param comparison   the comparison
    * @param compareValue the value to compare against.
    * @param trueValue    the value to set when the comparison is true.
    * @param falseValue   the value to set when the comparison is false.
    * @return the ObjectNDArray
    */
   public static <T> ObjectNDArray<T> test(@NonNull ObjectNDArray<T> n,
                                           @NonNull BiPredicate<T, T> comparison,
                                           T compareValue,
                                           T trueValue,
                                           T falseValue) {
      return n.map(d -> comparison.test(d, compareValue) ? trueValue : falseValue);
   }

   /**
    * <p>Tests the values in the given NDArray using the given <code>comparison</code> against the given
    * <code>threshold</code>, setting the value to <code>thresholded</code> when the threshold is met and keeps the
    * current value when not met. Results are stored in a new NDArray</p>
    *
    * @param n                the NDArray
    * @param comparison       the comparison
    * @param threshold        the threshold
    * @param thresholdedValue the thresholded value
    * @return the NumericNDArray
    */
   public static NumericNDArray test(@NonNull NumericNDArray n,
                                     @NonNull NumericComparison comparison,
                                     double threshold,
                                     double thresholdedValue) {
      return n.map(d -> comparison.compare(d, threshold) ? thresholdedValue : d);
   }

   /**
    * <p>Stack arrays in sequence horizontally (row wise).</p>
    *
    * @param <T>    the NDArray Type parameter
    * @param arrays the arrays to stack
    * @return the stacked NADArray
    */
   @SafeVarargs
   public static <T extends NDArray> T vstack(@NonNull T... arrays) {
      return vstack(List.of(arrays));
   }

   /**
    * <p>Stack arrays in sequence horizontally (row wise).</p>
    *
    * @param <T>    the NDArray Type parameter
    * @param arrays the arrays to stack
    * @return the stacked NADArray
    */
   public static <T extends NDArray> T vstack(@NonNull List<T> arrays) {
      return concatenate(Shape.ROW, arrays);
   }


}//END OF nd
