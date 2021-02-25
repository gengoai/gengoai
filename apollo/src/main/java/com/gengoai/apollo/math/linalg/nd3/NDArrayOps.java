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

import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

class NDArrayOps {


   static <T> NDArray<T> ensureNDArray(NDArray<?> source,
                                       NDArray<?> target,
                                       boolean copy) {
      if (target == null) {
         target = copy ? source.copy() : source.zeroLike();
      }
      return Cast.as(target);
   }

   static <T> NDArray<T> map(NDArray<T> lhs,
                             T value,
                             @NonNull BinaryOperator<T> operator,
                             NDArray<T> target) {
      NDArray<T> out = ensureNDArray(lhs, target, false);
      for (int i = 0; i < lhs.length(); i++) {
         out.set(i, operator.apply(lhs.get(i), value));
      }
      return out;
   }


   static <T> NDArray<T> map(NDArray<T> lhs,
                             int axis,
                             int position,
                             T value,
                             @NonNull BinaryOperator<T> operator,
                             NDArray<T> target) {
      int absAxis = lhs.shape().toAbsolute(axis);
      if (position < lhs.shape().get(absAxis)) {
         NDArray<T> out = ensureNDArray(lhs, target, false);
         lhs.shape().iterateAlong(absAxis, position)
            .forEach(ii -> out.set(ii, operator.apply(lhs.get(ii), value)));
         return out;
      }
      throw new IllegalArgumentException("Unable to map along axis (" +
                                               axis +
                                               ") at position  (" +
                                               position +
                                               ")  for shape "
                                               + lhs.shape());
   }

   static <T> NDArray<T> map(NDArray<T> lhs,
                             NDArray<? extends T> rhs,
                             @NonNull BinaryOperator<T> operator,
                             NDArray<T> target) {
      var broadcast = lhs.shape().accepts(rhs.shape());
      if (broadcast == Broadcast.ERROR) {
         throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
      }

      switch (broadcast) {
         case EMPTY:
            return lhs.copy();

         case MATRIX_COLUMN:
         case TENSOR_COLUMN:
            return map(lhs, Shape.COLUMN, rhs, operator, target);

         case MATRIX_ROW:
         case TENSOR_ROW:
            return map(lhs, Shape.ROW, rhs, operator, target);

         case SCALAR:
            return map(lhs, rhs.scalar(), operator, target);

         case TENSOR_CHANNEL:
         case TENSOR_KERNEL:
         case VECTOR:
         case MATRIX:
         case TENSOR: {
            NDArray<T> out = ensureNDArray(lhs, target, false);
            for (Index index : lhs.shape().range()) {
               out.set(index, operator.apply(lhs.get(index), rhs.get(rhs.shape().broadcast(index))));
            }
            return out;
         }

      }

      throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
   }

   static <T> NDArray<T> map(NDArray<T> lhs,
                             int axis,
                             NDArray<? extends T> rhs,
                             @NonNull BinaryOperator<T> operator,
                             NDArray<T> target) {

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
            return map(lhs, rhs.scalar(), operator, target);

         case VECTOR:
            return map(lhs, rhs, operator, target);

         case MATRIX_ROW:
         case MATRIX_COLUMN: {
            int cmpAxis = absAxis == Shape.ROW ? Shape.COLUMN : Shape.ROW;
            NDArray<T> out = ensureNDArray(lhs, target, false);
            for (Index index : lhsShape.range()) {
               out.set(index, operator.apply(lhs.get(index),
                                             rhs.get(rhs.shape().broadcast(index).get(cmpAxis))));
            }
            return out;
         }

         case TENSOR_ROW:
         case TENSOR_COLUMN: {
            NDArray<T> out = ensureNDArray(lhs, target, false);
            for (Index index : lhsShape.sliceIterator()) {
               try {
                  map(lhs.slice(index),
                      axis,
                      rhs.slice(index),
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

   static <T> NDArray<T> map(NDArray<T> lhs,
                             int axis,
                             int position,
                             NDArray<? extends T> rhs,
                             @NonNull BinaryOperator<T> operator,
                             NDArray<T> target) {

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
            return map(lhs, axis, position, rhs.scalar(), operator, target);
         case VECTOR:
            return map(lhs, rhs, operator, target);
         case MATRIX_ROW:
         case MATRIX_COLUMN: {
            int cmpAxis = absAxis == Shape.ROW ? Shape.COLUMN : Shape.ROW;
            NDArray<T> out = ensureNDArray(lhs, target, false);
            Index start = Index.zero().set(absAxis, position);
            Index end = lhsShape.asIndex(absAxis, position + 1);
            for (Index index : start.boundedIteratorTo(end)) {
               out.set(index, operator.apply(lhs.get(index), rhs.get(rhsShape.broadcast(index).get(cmpAxis))));
            }
            return out;
         }

         case TENSOR_ROW:
         case TENSOR_COLUMN: {
            final var rhsSlice = rhs.slice(0);
            NDArray<T> out = ensureNDArray(lhs, target, true);
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

   static <T> NDArray<T> mapDouble(NDArray<T> lhs,
                                   NDArray<?> rhs,
                                   @NonNull DoubleBinaryOperator operator,
                                   NDArray<T> target) {
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
            NDArray<T> out = ensureNDArray(lhs, target, false);
            for (Index index : lhs.shape().range()) {
               out.set(index, operator
                     .applyAsDouble(lhs.getDouble(index), rhs.getDouble(rhs.shape().broadcast(index))));
            }
            return out;
         }

      }

      throw new IllegalArgumentException(unableToBroadcast(rhs.shape(), lhs.shape(), broadcast));
   }

   static <T> NDArray<T> mapDouble(NDArray<T> lhs,
                                   int axis,
                                   NDArray<?> rhs,
                                   @NonNull DoubleBinaryOperator operator,
                                   NDArray<T> target) {

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
            NDArray<T> out = ensureNDArray(lhs, target, false);
            for (Index index : lhsShape.range()) {
               out.set(index, operator.applyAsDouble(lhs.getDouble(index),
                                                     rhs.getDouble(rhsShape.broadcast(index).get(cmpAxis))));
            }
            return out;
         }

         case TENSOR_ROW:
         case TENSOR_COLUMN: {
            NDArray<T> out = ensureNDArray(lhs, target, false);
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

   static <T> NDArray<T> mapDouble(NDArray<T> lhs,
                                   int axis,
                                   int position,
                                   NDArray<?> rhs,
                                   @NonNull DoubleBinaryOperator operator,
                                   NDArray<T> target) {

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
            NDArray<T> out = ensureNDArray(lhs, target, true);
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
            NDArray<T> out = ensureNDArray(lhs, target, true);
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

   static <T> NDArray<T> mapDouble(NDArray<T> lhs,
                                   int axis,
                                   int axisValue,
                                   double value,
                                   @NonNull DoubleBinaryOperator operator,
                                   NDArray<T> target) {
      int absAxis = lhs.shape().toAbsolute(axis);
      if (axisValue < lhs.shape().get(absAxis)) {
         NDArray<T> out = ensureNDArray(lhs, target, true);
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

   static <T> NDArray<T> mapDouble(NDArray<T> lhs,
                                   double value,
                                   @NonNull DoubleBinaryOperator operator,
                                   NDArray<T> target) {
      if (lhs.isEmpty()) {
         return lhs.copy();
      }
      NDArray<T> out = ensureNDArray(lhs, target, false);
      for (int i = 0; i < lhs.length(); i++) {
         out.set(i, operator.applyAsDouble(lhs.getDouble(i), value));
      }
      return out;
   }

   static <T> NDArray<T> mapDoubleSliceRange(NDArray<T> lhs,
                                             NDArray<?> rhs,
                                             IndexRange range,
                                             DoubleBinaryOperator operator,
                                             NDArray<T> target) {
      var lhsShape = lhs.shape();
      var rhsShape = rhs.shape();
      NDArray<T> out = ensureNDArray(lhs, target, true);
      for (Index index : range) {
         try {
            mapDouble(lhs.slice(index), rhs.slice(index), operator, out.slice(index));
         } catch (RuntimeException e) {
            throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, Broadcast.ERROR));
         }
      }
      return out;
   }

   static <T> NDArray<T> mapSlice(Shape outputShape, NDArray<T> lhs, UnaryOperator<NDArray<T>> operator) {
      NDArray<T> out = lhs.factory().zeros(outputShape);
      for (int i = 0; i < lhs.shape().sliceLength(); i++) {
         out.setSlice(i, operator.apply(lhs.slice(i)));
      }
      return out;
   }

   static <T> NDArray<T> mapSlice(Shape outputShape, NDArray<T> lhs, NDArray<?> rhs, BiFunction<NDArray<T>, NDArray<?>, NDArray<T>> operator) {
      NDArray<T> out = lhs.factory().zeros(outputShape);
      for (int i = 0; i < lhs.shape().sliceLength(); i++) {
         int rslice = rhs.shape().sliceLength() == 1 ? 0 : i;
         out.setSlice(i, operator.apply(lhs.slice(i), rhs.slice(rslice)));
      }
      return out;
   }

   static <V, T> NDArray<V> mapSlice(NDArrayFactory<V> factory,
                                     NDArray<T> lhs,
                                     Function<NDArray<T>, NDArray<V>> converter) {
      List<NDArray<V>> slices = new ArrayList<>();
      for (int i = 0; i < lhs.shape().sliceLength(); i++) {
         slices.add(converter.apply(lhs.slice(i)));
      }
      if (slices.isEmpty()) {
         return factory.empty();
      }
      NDArray<V> out = factory.zeros(lhs.shape().with(Shape.ROW, slices.get(0).shape().rows(),
                                                         Shape.COLUMN, slices.get(0).shape().columns()));
      for (int i = 0; i < slices.size(); i++) {
         out.setSlice(i, slices.get(i));
      }
      return out;
   }

   static <T> NDArray<T> mapSliceRange(NDArray<T> lhs,
                                       NDArray<? extends T> rhs,
                                       IndexRange range,
                                       BinaryOperator<T> operator,
                                       NDArray<T> target) {
      var lhsShape = lhs.shape();
      var rhsShape = rhs.shape();
      NDArray<T> out = ensureNDArray(lhs, target, true);
      for (Index index : range) {
         try {
            map(lhs.slice(index), rhs.slice(index), operator, out.slice(index));
         } catch (RuntimeException e) {
            throw new IllegalArgumentException(unableToBroadcast(rhsShape, lhsShape, Broadcast.ERROR));
         }
      }
      return out;
   }

   static <T> NDArray<T> reduceDoubleAxis(NDArray<T> lhs,
                                          DoubleBinaryOperator operator,
                                          int axis,
                                          int... other) {
      return reduceDoubleAxis(lhs, lhs.factory(), operator, axis, other);
   }

   static <T, V> NDArray<V> reduceDoubleAxis(NDArray<T> lhs,
                                             NDArrayFactory<V> factory,
                                             DoubleBinaryOperator operator,
                                             int axis,
                                             int... other) {
      if (lhs.isEmpty()) {
         return factory.empty();
      }
      if (lhs.shape().isScalar()) {
         return factory.zeros(1).fill(lhs.scalarDouble());
      }
      Validator.checkAxis(axis, lhs);
      for (int i : other) {
         Validator.checkAxis(i, lhs);
      }
      Shape s = lhs.shape().remove(axis, other);
      NDArray<V> o = factory.zeros(s);
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
