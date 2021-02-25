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

package com.gengoai.apollo.math.linalg.nd3;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.nd3.dense.*;
import com.gengoai.apollo.math.linalg.nd3.sparse.SparseFloat32Factory;
import com.gengoai.apollo.math.linalg.nd3.sparse.SparseStringNDArrayFactory;
import com.gengoai.conversion.Cast;
import com.gengoai.math.Math2;
import com.gengoai.math.NumericComparison;
import lombok.NonNull;
import org.tensorflow.Tensor;

import java.util.List;
import java.util.function.BiPredicate;

import static com.gengoai.apollo.math.linalg.nd3.NDArrayFactory.probe;

public final class nd {
   public static final DenseInt32Factory DINT32 = new DenseInt32Factory();
   public static final DenseInt64Factory DINT64 = new DenseInt64Factory();
   public static final DenseFloat32Factory DFLOAT32 = new DenseFloat32Factory();
   public static final SparseFloat32Factory SFLOAT32 = new SparseFloat32Factory();
   public static final DenseStringNDArrayFactory DSTRING = new DenseStringNDArrayFactory();
   public static final SparseStringNDArrayFactory SSTRING = new SparseStringNDArrayFactory();

   private nd() {
      throw new IllegalAccessError();
   }

   public static <T extends Number> NDArray<T> abs(@NonNull NDArray<T> n) {
      return n.mapDouble(Math::abs);
   }

   public static <T> NDArray<T> array(Object data, @NonNull Class<T> dType) {
      return NDArrayFactory.forType(dType).array(data);
   }

   public static <T> NDArray<T> array(Object data) {
      return Cast.as(NDArrayFactory.forType(probe(data).v2).array(data));
   }

   public static <T> NDArray<T> array(@NonNull Shape shape, Object data, @NonNull Class<T> dType) {
      return NDArrayFactory.forType(dType).array(shape, data);
   }

   public static <T> NDArray<T> array(@NonNull Shape shape, Object data) {
      return Cast.as(NDArrayFactory.forType(probe(data).v2).array(shape, data));
   }

   public static <T extends Number> NDArray<T> ceil(@NonNull NDArray<T> n) {
      return n.mapDouble(Math::ceil);
   }

   public static NDArray<?> convertTensor(@NonNull Tensor<?> tensor) {
      switch (tensor.dataType()) {
         case FLOAT:
            return Cast.as(DenseFloat32NDArray.fromTensor(tensor));
         case DOUBLE:
            return Cast.as(DenseFloat32NDArray.fromTensor(tensor));
         case INT32:
            return Cast.as(DenseInt32NDArray.fromTensor(tensor));
         case INT64:
            return Cast.as(DenseInt64NDArray.fromTensor(tensor));
         case STRING:
            return Cast.as(DenseStringNDArray.fromTensor(tensor));
         case BOOL:

      }
      throw new IllegalArgumentException("Cannot create NDArray from Tensor of type '" + tensor.dataType() + "'");
   }

   public static <T extends Number, V extends Number> NDArray<T> dot(@NonNull NDArray<T> a, @NonNull NDArray<V> b) {
      var broadCast = a.shape().accepts(b.shape());
      switch (broadCast) {
         case EMPTY:
            return a.copy();
         case SCALAR:
            return a.mul(b.scalarDouble());
         case VECTOR:
            if (b.shape().rows() == 1) {
               return a.mmul(b);
            }
            return a.factory().zeros(1).fill(a.dot(b));
         case MATRIX:
         case TENSOR:
         case TENSOR_CHANNEL:
         case TENSOR_KERNEL:
            return a.mmul(b);
         case MATRIX_ROW:
            if (a.shape().columns() == b.shape().rows()) {
               return a.mmul(b);
            }
         case TENSOR_ROW:
            return a.dot(b, Shape.ROW);
         case MATRIX_COLUMN:
         case TENSOR_COLUMN:
            if (a.shape().columns() == b.shape().rows()) {
               System.out.println("MMUL");
               return a.mmul(b);
            }
            return a.dot(b, Shape.COLUMN);
      }
      if (b.shape().isVector() && b.shape().length() == a.shape().rows()) {
         return a.dot(b.T(), Shape.COLUMN);
      }
      if (b.shape().isVector() && b.shape().length() == a.shape().columns()) {
         return a.dot(b.T(), Shape.ROW);
      }
      throw new IllegalArgumentException(NDArrayOps.unableToBroadcast(b.shape(), a.shape(), broadCast));
   }

   @SafeVarargs
   public static <T> NDArray<T> dstack(@NonNull NDArray<T>... arrays) {
      return dstack(List.of(arrays));
   }

   public static <T> NDArray<T> dstack(@NonNull List<NDArray<T>> arrays) {
      Validation.checkArgument(arrays.size() > 0);
      int channels = arrays.stream().mapToInt(n -> Math.max(1, n.shape().channels())).sum();
      Shape s = arrays.get(0).shape().with(Shape.CHANNEL, channels);
      NDArray<T> out = arrays.get(0).factory().zeros(s);
      int channelOffset = 0;
      for (NDArray<T> array : arrays) {
         if (!s.matrixShape().equals(array.shape().matrixShape())) {
            throw new IllegalArgumentException();
         }
         for (Index index : array.shape().range()) {
            T value = array.get(index);
            index.set(Shape.CHANNEL, index.getChannel() + channelOffset);
            out.set(index, value);
         }
         channelOffset += Math.max(1, array.shape().channels());
      }
      return out;
   }

   public static <T extends Number> NDArray<T> exp(@NonNull NDArray<T> n) {
      return n.mapDouble(Math::exp);
   }

   public static <T extends Number> NDArray<T> floor(@NonNull NDArray<T> n) {
      return n.mapDouble(Math::floor);
   }

   @SafeVarargs
   public static <T> NDArray<T> hstack(@NonNull NDArray<T>... arrays) {
      return hstack(List.of(arrays));
   }

   public static <T> NDArray<T> hstack(@NonNull List<NDArray<T>> arrays) {
      Validation.checkArgument(arrays.size() > 0);
      int columns = arrays.stream().mapToInt(n -> n.shape().columns()).sum();
      Shape s = arrays.get(0).shape().with(Shape.COLUMN, columns);
      NDArray<T> out = arrays.get(0).factory().zeros(s);
      int columnOffset = 0;
      for (NDArray<T> array : arrays) {
         for (Index index : array.shape().range()) {
            T value = array.get(index);
            index.set(Shape.COLUMN, index.getColumn() + columnOffset);
            out.set(index, value);
         }
         columnOffset += array.shape().columns();
      }
      return out;
   }

   public static <T extends Number> NDArray<T> log(@NonNull NDArray<T> n) {
      return n.mapDouble(Math2::safeLog);
   }

   public static <T extends Number> NDArray<T> log10(@NonNull NDArray<T> n) {
      return n.mapDouble(Math::log10);
   }

   public static <T extends Number> NDArray<T> pow(@NonNull NDArray<T> n, int power) {
      return n.mapDouble(d -> Math.pow(d, power));
   }

   public static <T extends Number> NDArray<T> round(@NonNull NDArray<T> n) {
      return n.mapDouble(Math::round);
   }

   public static <T extends Number> NDArray<T> sigmoid(@NonNull NDArray<T> n) {
      return n.mapDouble(x -> 1.0 / (1.0 + Math.exp(-x)));
   }

   public static <T extends Number> NDArray<T> signum(@NonNull NDArray<T> n) {
      return n.mapDouble(Math::signum);
   }

   public static <T extends Number> NDArray<T> sqrt(@NonNull NDArray<T> n) {
      return n.mapDouble(Math::sqrt);
   }

   public static <T extends Number> NDArray<T> square(@NonNull NDArray<T> n) {
      return n.mapDouble(d -> d * d);
   }

   public static <T extends Number> NDArray<T> test(@NonNull NDArray<T> n,
                                                    @NonNull NumericComparison comparison,
                                                    double threshold,
                                                    double thresholdedValue,
                                                    double nonThresholdedValue) {
      return n.mapDouble(d -> comparison.compare(d, threshold) ? thresholdedValue : nonThresholdedValue);
   }

   public static <T> NDArray<T> test(@NonNull NDArray<T> n,
                                     @NonNull BiPredicate<T, T> comparison,
                                     T compareValue,
                                     T trueValue) {
      return n.map(d -> comparison.test(d, compareValue) ? trueValue : d);
   }

   public static <T> NDArray<T> test(@NonNull NDArray<T> n,
                                     @NonNull BiPredicate<T, T> comparison,
                                     T compareValue,
                                     T trueValue,
                                     T falseValue) {
      return n.map(d -> comparison.test(d, compareValue) ? trueValue : falseValue);
   }

   public static <T extends Number> NDArray<T> test(@NonNull NDArray<T> n,
                                                    @NonNull NumericComparison comparison,
                                                    double threshold,
                                                    double thresholdedValue) {
      return n.mapDouble(d -> comparison.compare(d, threshold) ? thresholdedValue : d);
   }

   @SafeVarargs
   public static <T> NDArray<T> vstack(@NonNull NDArray<T>... arrays) {
      return vstack(List.of(arrays));
   }

   public static <T> NDArray<T> vstack(@NonNull List<NDArray<T>> arrays) {
      Validation.checkArgument(arrays.size() > 0);
      int rows = arrays.stream().mapToInt(n -> Math.max(1, n.shape().rows())).sum();
      Shape s = arrays.get(0).shape().with(Shape.ROW, rows);
      NDArray<T> out = arrays.get(0).factory().zeros(s);
      int rowOffset = 0;
      for (NDArray<T> array : arrays) {
         for (Index index : array.shape().range()) {
            T value = array.get(index);
            index.set(Shape.ROW, index.getRow() + rowOffset);
            out.set(index, value);
         }
         rowOffset += Math.max(1, array.shape().rows());
      }
      return out;
   }


}//END OF nd
