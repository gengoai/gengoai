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

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;

class NDArrayOps {


   static NumericNDArray ensureNDArray(NumericNDArray source,
                                       NumericNDArray target,
                                       boolean copy) {
      if (target == null) {
         target = copy ? source.copy() : source.zeroLike();
      }
      return Cast.as(target);
   }


   static <T extends NDArray> T ensureNDArray(T source,
                                              T target,
                                              boolean copy) {
      if (target == null) {
         target = copy ? Cast.as(source.copy()) : Cast.as(source.zeroLike());
      }
      return target;
   }

   static <T extends NDArray, V> T map(T lhs,
                                       V value,
                                       @NonNull BinaryOperator<V> operator,
                                       T target) {
      T out = ensureNDArray(lhs, target, false);
      for (int i = 0; i < lhs.length(); i++) {
         out.set(i, operator.apply(Cast.as(lhs.get(i)), value));
      }
      return out;
   }

   static <T extends NDArray, V> T map(T lhs,
                                       int axis,
                                       int position,
                                       V value,
                                       @NonNull BinaryOperator<V> operator,
                                       T target) {
      int absAxis = lhs.shape().toAbsolute(axis);
      if (position < lhs.shape().get(absAxis)) {
         T out = ensureNDArray(lhs, target, true);
         lhs.shape().iterateAlong(absAxis, position)
            .forEach(ii -> out.set(ii, operator.apply(Cast.as(lhs.get(ii)), Cast.as(value))));
         return out;
      }
      throw new IllegalArgumentException("Unable to map along axis (" +
                                               axis +
                                               ") at position  (" +
                                               position +
                                               ")  for shape "
                                               + lhs.shape());
   }

   static <T extends NDArray, V> T map(T lhs,
                                       NDArray rhs,
                                       @NonNull BinaryOperator<V> operator,
                                       T target) {
      var broadcast = lhs.shape().accepts(rhs.shape());
      if (broadcast == Broadcast.ERROR) {
         throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
      }

      switch (broadcast) {
         case EMPTY:
            return Cast.as(lhs.copy());

         case MATRIX_COLUMN:
         case TENSOR_COLUMN:
            return map(lhs, Shape.COLUMN, rhs, operator, target);

         case MATRIX_ROW:
         case TENSOR_ROW:
            return map(lhs, Shape.ROW, rhs, operator, target);

         case SCALAR:
            return map(lhs, Cast.<V>as(rhs.scalar()), operator, target);

         case TENSOR_CHANNEL:
         case TENSOR_KERNEL:
         case VECTOR:
         case MATRIX:
         case TENSOR: {
            T out = ensureNDArray(lhs, target, false);
            for (Index index : lhs.shape().range()) {
               out.set(index, operator.apply(Cast.as(lhs.get(index)), Cast.as(rhs.get(rhs.shape().broadcast(index)))));
            }
            return out;
         }

      }

      throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
   }

   static <T extends NDArray, V> T map(T lhs,
                                       int axis,
                                       NDArray rhs,
                                       @NonNull BinaryOperator<V> operator,
                                       T target) {

      var broadcast = lhs.shape().accepts(rhs.shape());
      if (broadcast == Broadcast.ERROR) {
         throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
      }

      var lhsShape = lhs.shape();
      var rhsShape = rhs.shape();
      int absAxis = lhsShape.toAbsolute(axis);

      switch (broadcast) {
         case EMPTY:
            return Cast.as(lhs.copy());

         case SCALAR:
            return map(lhs, Cast.<V>as(rhs.scalar()), operator, target);

         case VECTOR:
            return map(lhs, rhs, operator, target);

         case MATRIX_ROW:
         case MATRIX_COLUMN: {
            int cmpAxis = absAxis == Shape.ROW ? Shape.COLUMN : Shape.ROW;
            T out = ensureNDArray(lhs, target, false);
            for (Index index : lhsShape.range()) {
               V l = Cast.as(lhs.get(index));
               V r = Cast.as(rhs.get(rhs.shape().broadcast(index).get(cmpAxis)));
               out.set(index, operator.apply(l, r));
            }
            return out;
         }

         case TENSOR_ROW:
         case TENSOR_COLUMN: {
            T out = ensureNDArray(lhs, target, false);
            for (Index index : lhsShape.sliceIterator()) {
               try {
                  map(lhs.slice(index),
                      axis,
                      rhs.slice(index),
                      operator,
                      out.slice(index));
               } catch (RuntimeException e) {
                  e.printStackTrace();
                  throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, broadcast));
               }
            }
            return out;
         }

         case TENSOR_CHANNEL:
         case TENSOR_KERNEL: {
            return mapSliceRange(lhs,
                                 rhs,
                                 lhsShape.sliceIterator(),
                                 operator,
                                 target);
         }

      }

      throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, broadcast) +
                                               " for axis '" +
                                               absAxis +
                                               "'");
   }

   static <T extends NDArray, V> T map(T lhs,
                                       int axis,
                                       int position,
                                       NDArray rhs,
                                       @NonNull BinaryOperator<V> operator,
                                       T target) {

      var broadcast = lhs.shape().accepts(rhs.shape());
      if (broadcast == Broadcast.ERROR) {
         throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
      }

      var lhsShape = lhs.shape();
      var rhsShape = rhs.shape();
      int absAxis = lhsShape.toAbsolute(axis);

      switch (broadcast) {
         case EMPTY:
            return Cast.as(lhs.copy());
         case SCALAR:
            return map(lhs, axis, position, Cast.<V>as(rhs.scalar()), operator, target);
         case VECTOR:
            return map(lhs, rhs, operator, target);
         case MATRIX_ROW:
         case MATRIX_COLUMN: {
            int cmpAxis = absAxis == Shape.ROW ? Shape.COLUMN : Shape.ROW;
            T out = ensureNDArray(lhs, target, false);
            Index start = Index.zero().set(absAxis, position);
            Index end = lhsShape.asIndex(absAxis, position + 1);
            for (Index index : start.boundedIteratorTo(end)) {
               V l = Cast.as(lhs.get(index));
               V r = Cast.as(rhs.get(rhs.shape().broadcast(index).get(cmpAxis)));
               out.set(index, operator.apply(l, r));
            }
            return out;
         }

         case TENSOR_ROW:
         case TENSOR_COLUMN: {
            final var rhsSlice = rhs.slice(0);
            T out = ensureNDArray(lhs, target, true);
            for (Index index : lhsShape.sliceIterator()) {
               try {
                  map(lhs.slice(index.getKernel(), index.getChannel()),
                      absAxis,
                      position,
                      rhsSlice,
                      operator,
                      out.slice(index.getKernel(), index.getChannel()));
               } catch (RuntimeException e) {
                  e.printStackTrace();
                  throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, Broadcast.ERROR));
               }
            }
            return out;
         }

         case TENSOR_CHANNEL:
         case TENSOR_KERNEL: {
            return mapSliceRange(lhs,
                                 rhs,
                                 lhsShape.sliceIterator(absAxis, position),
                                 operator,
                                 target);
         }
      }


      throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, Broadcast.ERROR) +
                                               " along axis (" +
                                               axis +
                                               ") at position (" +
                                               position +
                                               ")");
   }

   static NumericNDArray mapDouble(NumericNDArray lhs,
                                   NumericNDArray rhs,
                                   @NonNull DoubleBinaryOperator operator,
                                   NumericNDArray target) {
      var broadcast = lhs.shape().accepts(rhs.shape());
      if (broadcast == Broadcast.ERROR) {
         throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
      }

      switch (broadcast) {
         case EMPTY:
            return lhs.copy();

         case MATRIX_COLUMN:
         case TENSOR_COLUMN:
            return mapDouble(lhs, Shape.COLUMN, rhs, operator, target);

         case MATRIX_ROW:
         case TENSOR_ROW:
            return mapDouble(lhs, Shape.ROW, rhs, operator, target);

         case SCALAR:
            return mapDouble(lhs, rhs.scalarDouble(), operator, target);

         case TENSOR_CHANNEL:
         case TENSOR_KERNEL:
         case VECTOR:
         case MATRIX:
         case TENSOR: {
            NumericNDArray out = ensureNDArray(lhs, target, false);
            for (Index index : lhs.shape().range()) {
               out.set(index, operator
                     .applyAsDouble(lhs.getDouble(index), rhs.getDouble(rhs.shape().broadcast(index))));
            }
            return out;
         }

      }

      throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
   }

   static NumericNDArray mapDouble(NumericNDArray lhs,
                                   int axis,
                                   NumericNDArray rhs,
                                   @NonNull DoubleBinaryOperator operator,
                                   NumericNDArray target) {

      var broadcast = lhs.shape().accepts(rhs.shape());
      if (broadcast == Broadcast.ERROR) {
         throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
      }
      var lhsShape = lhs.shape();
      var rhsShape = rhs.shape();
      int absAxis = lhsShape.toAbsolute(axis);

      switch (broadcast) {

         case SCALAR:
            return mapDouble(lhs, rhs.scalarDouble(), operator, target);

         case VECTOR:
            return mapDouble(lhs, rhs, operator, target);

         case MATRIX_ROW:
         case MATRIX_COLUMN: {
            int cmpAxis = absAxis == Shape.ROW ? Shape.COLUMN : Shape.ROW;
            NumericNDArray out = ensureNDArray(lhs, target, false);
            for (Index index : lhsShape.range()) {
               out.set(index, operator.applyAsDouble(lhs.getDouble(index),
                                                     rhs.getDouble(rhsShape.broadcast(index).get(cmpAxis))));
            }
            return out;
         }

         case TENSOR_ROW:
         case TENSOR_COLUMN: {
            NumericNDArray out = ensureNDArray(lhs, target, false);
            for (Index index : lhsShape.sliceIterator()) {
               try {
                  mapDouble(lhs.slice(index),
                            axis,
                            rhs.slice(rhs.shape().broadcast(index)),
                            operator,
                            out.slice(index));
               } catch (RuntimeException e) {
                  throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, broadcast));
               }
            }
            return out;
         }

         case TENSOR_CHANNEL:
         case TENSOR_KERNEL: {
            return mapDoubleSliceRange(lhs,
                                       rhs,
                                       lhsShape.sliceIterator(),
                                       operator,
                                       target);
         }

      }

      throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, broadcast) +
                                               " for axis '" +
                                               absAxis +
                                               "'");
   }

   static NumericNDArray mapDouble(NumericNDArray lhs,
                                   int axis,
                                   int position,
                                   NumericNDArray rhs,
                                   @NonNull DoubleBinaryOperator operator,
                                   NumericNDArray target) {

      var broadcast = lhs.shape().accepts(rhs.shape());
      if (broadcast == Broadcast.ERROR) {
         throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
      }

      var lhsShape = lhs.shape();
      var rhsShape = rhs.shape();
      int absAxis = lhsShape.toAbsolute(axis);

      switch (broadcast) {
         case EMPTY:
            return lhs.copy();
         case SCALAR:
            return mapDouble(lhs, axis, position, rhs.scalarDouble(), operator, target);
         case VECTOR:
            return mapDouble(lhs, rhs, operator, target);

         case MATRIX_ROW:
         case MATRIX_COLUMN: {
            int cmpAxis = absAxis == Shape.ROW ? Shape.COLUMN : Shape.ROW;
            NumericNDArray out = ensureNDArray(lhs, target, true);
            Index start = Index.zero().set(absAxis, position);
            Index end = lhsShape.asIndex(absAxis, position + 1);
            for (Index index : start.boundedIteratorTo(end)) {
               out.set(index, operator.applyAsDouble(lhs.getDouble(index),
                                                     rhs.getDouble(rhsShape.broadcast(index).get(cmpAxis))));
            }
            return out;
         }

         case TENSOR_ROW:
         case TENSOR_COLUMN: {
            final var rhsSlice = rhs.slice(0);
            NumericNDArray out = ensureNDArray(lhs, target, true);
            for (Index index : lhsShape.sliceIterator()) {
               try {
                  mapDouble(lhs.slice(index.getKernel(), index.getChannel()),
                            absAxis,
                            position,
                            rhsSlice,
                            operator,
                            out.slice(index.getKernel(), index.getChannel()));
               } catch (RuntimeException e) {
                  e.printStackTrace();
                  throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, Broadcast.ERROR));
               }
            }
            return out;
         }

         case TENSOR_CHANNEL:
         case TENSOR_KERNEL: {
            return mapDoubleSliceRange(lhs,
                                       rhs,
                                       lhsShape.sliceIterator(absAxis, position),
                                       operator,
                                       target);
         }
      }


      throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, Broadcast.ERROR) +
                                               " along axis (" +
                                               axis +
                                               ") at position (" +
                                               position +
                                               ")");
   }

   static NumericNDArray mapDouble(NumericNDArray lhs,
                                   int axis,
                                   int axisValue,
                                   double value,
                                   @NonNull DoubleBinaryOperator operator,
                                   NumericNDArray target) {
      int absAxis = lhs.shape().toAbsolute(axis);
      if (axisValue < lhs.shape().get(absAxis)) {
         NumericNDArray out = ensureNDArray(lhs, target, true);
         Index start = Index.zero().set(absAxis, axisValue);
         Index end = lhs.shape().copy().asIndex(absAxis, axisValue + 1);
         start.boundedIteratorTo(end)
              .forEach(ii -> out.set(ii, operator.applyAsDouble(lhs.getDouble(ii), value)));
         return out;
      }
      throw new IllegalArgumentException("Unable to map along axis (" +
                                               axis +
                                               ") at position  (" +
                                               axisValue +
                                               ")  for shape "
                                               + lhs.shape());
   }

   static NumericNDArray mapDouble(NumericNDArray lhs,
                                   double value,
                                   @NonNull DoubleBinaryOperator operator,
                                   NumericNDArray target) {
      if (lhs.isEmpty()) {
         return lhs.copy();
      }
      NumericNDArray out = ensureNDArray(lhs, target, false);
      for (int i = 0; i < lhs.length(); i++) {
         out.set(i, operator.applyAsDouble(lhs.getDouble(i), value));
      }
      return out;
   }

   static NumericNDArray mapDoubleSliceRange(NumericNDArray lhs,
                                             NumericNDArray rhs,
                                             IndexRange range,
                                             DoubleBinaryOperator operator,
                                             NumericNDArray target) {
      var lhsShape = lhs.shape();
      var rhsShape = rhs.shape();
      NumericNDArray out = ensureNDArray(lhs, target, true);
      for (Index index : range) {
         try {
            mapDouble(lhs.slice(index), rhs.slice(index), operator, out.slice(index));
         } catch (RuntimeException e) {
            throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, Broadcast.ERROR));
         }
      }
      return out;
   }


   static NumericNDArray mapSlice(Shape outputShape,
                                  NumericNDArray lhs,
                                  NumericNDArray rhs,
                                  BiFunction<NumericNDArray, NumericNDArray, NumericNDArray> operator) {
      NumericNDArray out = lhs.factory().zeros(outputShape);
      for (int i = 0; i < lhs.shape().sliceLength(); i++) {
         int rslice = rhs.shape().sliceLength() == 1 ? 0 : i;
         out.setSlice(i, operator.apply(lhs.slice(i), rhs.slice(rslice)));
      }
      return out;
   }


   static <T extends NDArray, V> T mapSliceRange(T lhs,
                                                 NDArray rhs,
                                                 IndexRange range,
                                                 BinaryOperator<V> operator,
                                                 T target) {
      var lhsShape = lhs.shape();
      var rhsShape = rhs.shape();
      T out = ensureNDArray(lhs, target, true);
      for (Index index : range) {
         try {
            map(lhs.slice(index), rhs.slice(index), operator, out.slice(index));
         } catch (RuntimeException e) {
            throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, Broadcast.ERROR));
         }
      }
      return out;
   }

   static NumericNDArray reduceDoubleAxis(NumericNDArray lhs,
                                          DoubleBinaryOperator operator,
                                          int axis,
                                          int... other) {
      return reduceDoubleAxis(lhs, lhs.factory(), operator, axis, other);
   }

   static NumericNDArray reduceDoubleAxis(NumericNDArray lhs,
                                          NumericNDArrayFactory factory,
                                          DoubleBinaryOperator operator,
                                          int axis,
                                          int... other) {
      if (lhs.isEmpty()) {
         return factory.empty();
      }
      if (lhs.shape().isScalar()) {
         return factory.zeros(1).fill(lhs.scalarDouble());
      }
      NDArray.checkAxis(axis, lhs);
      for (int i : other) {
         NDArray.checkAxis(i, lhs);
      }
      Shape s = lhs.shape().remove(axis, other);
      NumericNDArray o = factory.zeros(s);
      for (Index index : lhs.shape().range()) {
         double v = lhs.getDouble(index);
         index = index.remove(axis, other);
         o.set(index, operator.applyAsDouble(o.getDouble(index), v));
      }
      return o;
   }

   static String unableToBroadcast(Shape rhs, Shape lhs, Broadcast broadcast) {
      if (broadcast == Broadcast.ERROR) {
         return String.format("Unable to broadcast NDArray of shape %s to an NDArray of shape %s",
                              rhs,
                              lhs);
      }
      return String.format("Unable to broadcast ('%s') NDArray of shape %s to an NDArray of shape %s",
                           broadcast,
                           rhs,
                           lhs);
   }
}
