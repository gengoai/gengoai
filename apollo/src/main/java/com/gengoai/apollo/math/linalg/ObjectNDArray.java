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

import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;


/**
 * <p>A base for NDArrays whose elements are Objects. Allows for manipulation of the objects through mapping
 * functions.</p>
 *
 * @param <T> the type parameter
 *
 * @author David B. Bracewell
 */
public abstract class ObjectNDArray<T> extends NDArray {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new ObjectNDArray.
    *
    * @param shape the shape of the NDArray
    */
   protected ObjectNDArray(Shape shape) {
      super(shape);
   }

   @Override
   public ObjectNDArray<T> T() {
      return Cast.as(super.T());
   }

   @Override
   public <Z> ObjectNDArray<Z> asObjectNDArray(@NonNull Class<Z> dType) {
      if (dType.isAssignableFrom(getType())) {
         return Cast.as(this);
      }
      return super.asObjectNDArray(dType);
   }

   @Override
   public ObjectNDArray<T> compact() {
      return Cast.as(super.compact());
   }

   @Override
   public ObjectNDArray<T> copy() {
      return Cast.as(super.copy());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj == this) {
         return true;
      }
      if (obj instanceof ObjectNDArray) {
         ObjectNDArray<?> r = Cast.as(obj);
         if (r.getType() == String.class && shape().equals(r.shape())) {
            for (Index index : shape().range()) {
               if (!get(index).equals(r.get(index))) {
                  return false;
               }
            }
            return true;
         }
      }
      return false;
   }

   @Override
   public ObjectNDArrayFactory<T> factory() {
      return Cast.as(super.factory());
   }

   @Override
   public ObjectNDArray<T> fill(Object value) {
      return Cast.as(super.fill(value));
   }

   /**
    * <p>Fills the NDArray with the given value for elements which pass the given predicate.</p>
    *
    * @param test  the predicate to test values to determine if the corresponding element should be filled with the
    *              given value
    * @param value the value to set all cells in the NDArray
    * @return This NDArray
    */
   public ObjectNDArray<T> fillIf(@NonNull Predicate<? super T> test, T value) {
      for (int i = 0; i < length(); i++) {
         if (test.test(get(i))) {
            set(i, value);
         }
      }
      return this;
   }

   /**
    * <p>Processes the sparse entries in this NDArray</p>
    *
    * @param consumer the consumer
    */
   public void forEachSparse(@NonNull EntryConsumer<T> consumer) {
      for (int i = 0; i < shape().length(); i++) {
         T value = get(i);
         if (value != null) {
            consumer.apply(i, get(i));
         }
      }
   }

   @Override
   public T get(long offset) {
      return get(shape().calculateIndex(offset));
   }

   @Override
   public T get(int row, int col) {
      return get(0, 0, row, col);
   }

   @Override
   public T get(int channel, int row, int col) {
      return get(0, channel, row, col);
   }

   @Override
   public abstract T get(int kernel, int channel, int row, int col);

   @Override
   public T get(@NonNull Index index) {
      return get(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn());
   }

   @Override
   public ObjectNDArray<T> get(@NonNull IndexRange range) {
      return Cast.as(super.get(range));
   }

   @Override
   public ObjectNDArray<T> getAxis(int axis, int position) {
      return Cast.as(super.getAxis(axis, position));
   }

   @Override
   public ObjectNDArray<T> getAxis(int axis, long[] positions) {
      return Cast.as(super.getAxis(axis, positions));
   }

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated using the given unary
    * operator.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   b = a.map(String::toUpperCase);
    *
    *   //Would result in b being
    *   {{ "A", "B", "C",
    *      "D", "E", "F" }}
    * }*
    * </pre>
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> map(@NonNull UnaryOperator<T> operator) {
      ObjectNDArray<T> out = zeroLike();
      for (int i = 0; i < length(); i++) {
         out.set(i, operator.apply(get(i)));
      }
      return out;
   }

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated by the given binary
    * operation with the given value. The operation is applied with this NDArray's value as the first (i.e. left)
    * argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   b = a.map("_c", String::concat);
    *
    *   //Would result in b being
    *   {{ "a_c", "b_c", "c_c",
    *      "d_c", "e_c", "f_c" }}
    * }*
    * </pre>
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> map(T value, @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, value, operator, null);
   }

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated by the given binary
    * operation with values in the given rhs NDArray The operation is applied with this NDArray's value as the first
    * (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   b = a.map({"_a", "_b", "_c", "_d", "_e", "_f"}, String::concat);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{ "a_a", "b_b", "c_c",
    *      "d_d", "e_e", "f_f" }}
    * }*
    * </pre>
    *
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> map(@NonNull ObjectNDArray<T> rhs, @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, rhs, operator, null);
   }

   /**
    * <p>Creates a new NDArray whose values are calculated using the given binary operator applied to the
    * values of this NDArray along the given <code>axis</code> with the values in the given NDArray. The operation is
    * applied with this NDArray's value as the first (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   b = a.mapAxis(Shape.COLUMN, {"_1", "_2"}, String::concat);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{ "a_1", "b_1", "c_1",
    *      "d_2", "e_2", "f_2" }}
    * }*
    * </pre>
    *
    * @param axis     the axis to map
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> map(int axis,
                               @NonNull ObjectNDArray<T> rhs,
                               @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, axis, rhs, operator, null);
   }

   /**
    * <p>Updates the values along the given <code>dimension</code> of the given <code>axis</code> of this
    * NDArray by performing the given binary operation with the given <code>value</code>. The operation is applied with
    * this NDArray's value as the first (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   b = a.mapAxis(Shape.COLUMN, 1, "_C", String::concat);
    *
    *   //Would result in b being
    *   {{ "a", "b_C", "c",
    *      "d", "e_C", "f" }}
    * }*
    * </pre>
    *
    * @param axis     the axis to map
    * @param position the dimension of the axis to be updated
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> map(int axis,
                               int position,
                               T value,
                               @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, axis, position, value, operator, null);
   }

   /**
    * <p>Updates the values along the given <code>position</code> of the given <code>axis</code> of this
    * NDArray by performing the given binary operation with the values in the given NDArray. The operation is applied
    * with this NDArray's value as the first (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   b = a.mapAxis(Shape.COLUMN, 1, {"_1", "_2}, String::concat);
    *   //Note the rhs NDArray only needs to have the correct length but not shape
    *
    *   //Would result in b being
    *   {{ "a", "b_1", "c",
    *      "d", "e_2", "f" }}
    * }*
    * </pre>
    *
    * @param axis     the axis to map
    * @param position the position of the axis to be updated
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> map(int axis,
                               int position,
                               ObjectNDArray<T> rhs,
                               @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, axis, position, rhs, operator, null);
   }

   /**
    * <p>Updates the values in this NDArray using the given unary operator.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   a.mapi(String::toUpperCase);
    *
    *   //Would result in a being
    *   {{ "A", "B", "C",
    *      "D", "E", "F" }}
    * }*
    * </pre>
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> mapi(@NonNull UnaryOperator<T> operator) {
      for (int i = 0; i < length(); i++) {
         set(i, operator.apply(get(i)));
      }
      return this;
   }

   /**
    * <p>Updates the values in this NDArray by applying the given binary operation to each element with the
    * given value. The operation is applied with this NDArray's value as the first (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   a.mapi("_c", String::concat);
    *
    *   //Would result in a being
    *   {{ "a_c", "b_c", "c_c",
    *      "d_c", "e_c", "f_c" }}
    * }*
    * </pre>
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> mapi(T value, @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, value, operator, this);
   }

   /**
    * <p>Updates the values in this NDArray by applying the given binary operation to each element with the
    * associated element in the <code>rhs</code> NDArray. The operation is applied with this NDArray's value as the
    * first (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   a.mapi({"_a", "_b", "_c", "_d", "_e", "_f"}, String::concat);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{ "a_a", "b_b", "c_c",
    *      "d_d", "e_e", "f_f" }}
    * }*
    * </pre>
    *
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> mapi(@NonNull ObjectNDArray<T> rhs, @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, rhs, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>axis</code> of this NDArray by performing the given binary
    * operation with the values in the given NDArray. The operation is applied with this NDArray's value as the first
    * (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   a.mapiAxis(Shape.COLUMN, {"_1", "_2"}, String::concat);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{ "a_1", "b_1", "c_1",
    *      "d_2", "e_2", "f_2" }}
    * }*
    * </pre>
    *
    * @param axis     the axis to map
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public ObjectNDArray<T> mapi(int axis,
                                @NonNull ObjectNDArray<T> rhs,
                                @NonNull BinaryOperator<T> operator) {
      checkAxis(axis, this);
      return NDArrayOps.map(this, axis, rhs, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>axis</code> at the given <code>position</code>  of this
    * NDArray by performing the given binary operation with the given value. The operation is applied with this
    * NDArray's value as the first (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   b = a.mapAxis(Shape.COLUMN, 1, "_C", String::concat);
    *
    *   //Would result in b being
    *   {{ "a", "b_C", "c",
    *      "d", "e_C", "f" }}
    * }*
    * </pre>
    *
    * @param axis     the axis to apply the operation on
    * @param position the axis position to apply the operation on
    * @param rhs      the right hand side value to use in the binary operation
    * @param operator the operation to perform on the values of this NDArray and the given rhs value
    * @return this NDArray
    */
   public ObjectNDArray<T> mapi(int axis,
                                int position,
                                T rhs,
                                @NonNull BinaryOperator<T> operator) {
//      checkAxis(axis, this);
//      checkDimension(axis, position, this);
      return NDArrayOps.map(this, axis, position, rhs, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>position</code> of the given <code>axis</code> of this
    * NDArray by performing the given binary operation with the values in the given NDArray. The operation is applied
    * with this NDArray's value as the first (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix of String.
    *    a = {{ "a", "b", "c",
    *           "d", "e", "f" }}
    *
    *   //Performing
    *   b = a.mapAxis(Shape.COLUMN, 1, {"_1", "_2}, String::concat);
    *   //Note the rhs NDArray only needs to have the correct length but not shape
    *
    *   //Would result in b being
    *   {{ "a", "b_1", "c",
    *      "d", "e_2", "f" }}
    * }*
    * </pre>
    *
    * @param axis     the axis to apply the operation on
    * @param position the axis position to apply the operation on
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return this NDArray
    */
   public ObjectNDArray<T> mapi(int axis,
                                int position,
                                ObjectNDArray<T> rhs,
                                @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, axis, position, rhs, operator, this);
   }

   @Override
   public ObjectNDArray<T> max(int axis, @NonNull int... other) {
      return Cast.as(super.max(axis, other));
   }

   @Override
   public T max() {
      return Cast.as(super.max());
   }

   @Override
   public ObjectNDArray<T> min(int axis, int... other) {
      return Cast.as(super.min(axis, other));
   }

   @Override
   public T min() {
      return Cast.as(super.min());
   }


   @Override
   public ObjectNDArray<T> padPostWith(Object padValue, @NonNull Shape paddedShape) {
      ObjectNDArray<T> out = factory().create(paddedShape, NDArrayInitializer.constant(padValue));
      for (Index index : shape().range()) {
         if (paddedShape.contains(index)) {
            out.set(index, get(index));
         }
      }
      return out;
   }

   @Override
   public ObjectNDArray<T> padPost(int axis, int length) {
      return padPost(shape().with(axis, length));
   }

   @Override
   public ObjectNDArray<T> padPost(@NonNull int... axisLengthPairs) {
      return padPost(shape().with(axisLengthPairs));
   }

   @Override
   public ObjectNDArray<T> padPost(@NonNull Shape paddedShape) {
      ObjectNDArray<T> out = factory().zeros(paddedShape);
      for (Index index : shape().range()) {
         if (paddedShape.contains(index)) {
            out.set(index, get(index));
         }
      }
      return out;
   }

   @Override
   public abstract ObjectNDArray<T> reshape(@NonNull Shape newShape);

   @Override
   public ObjectNDArray<T> reshape(@NonNull int... dims) {
      return Cast.as(super.reshape(dims));
   }

   @Override
   public ObjectNDArray<T> set(long index, Object value) {
      return Cast.as(super.set(index, value));
   }

   @Override
   public ObjectNDArray<T> set(int row, int col, Object value) {
      return Cast.as(super.set(row, col, value));
   }

   @Override
   public ObjectNDArray<T> set(int channel, int row, int col, Object value) {
      return Cast.as(super.set(channel, row, col, value));
   }

   @Override
   public abstract ObjectNDArray<T> set(int kernel, int channel, int row, int col, Object value);

   @Override
   public ObjectNDArray<T> set(@NonNull Index index, Object value) {
      return Cast.as(super.set(index, value));
   }

   @Override
   public ObjectNDArray<T> setAxis(int axis, int position, @NonNull NDArray rhs) {
      return Cast.as(super.setAxis(axis, position, rhs));
   }

   @Override
   public ObjectNDArray<T> setAxis(int axis, int position, Object rhs) {
      return Cast.as(super.setAxis(axis, position, rhs));
   }

   @Override
   public ObjectNDArray<T> setLabel(Object label) {
      return Cast.as(super.setLabel(label));
   }

   @Override
   public ObjectNDArray<T> setPredicted(Object predicted) {
      return Cast.as(super.setPredicted(predicted));
   }

   @Override
   public ObjectNDArray<T> setRange(@NonNull IndexRange indexRange, @NonNull NDArray rhs) {
      return Cast.as(super.setRange(indexRange, rhs));
   }

   @Override
   public ObjectNDArray<T> setRange(@NonNull IndexRange indexRange, Object rhs) {
      return Cast.as(super.setRange(indexRange, rhs));
   }

   @Override
   public ObjectNDArray<T> setSlice(int index, @NonNull NDArray slice) {
      return Cast.as(super.setSlice(index, slice));
   }

   @Override
   public ObjectNDArray<T> setSlice(int kernel, int channel, @NonNull NDArray slice) {
      return setSlice(shape().calculateSliceIndex(kernel, channel), slice);
   }

   @Override
   public ObjectNDArray<T> setWeight(double weight) {
      return Cast.as(super.setWeight(weight));
   }

   @Override
   public abstract ObjectNDArray<T> slice(int index);

   @Override
   public abstract ObjectNDArray<T> slice(int startKernel, int startChannel, int endKernel, int endChannel);

   @Override
   public ObjectNDArray<T> slice(int kernel, int channel) {
      return slice(shape().calculateSliceIndex(kernel, channel));
   }

   @Override
   public ObjectNDArray<T> slice(@NonNull Index index) {
      return slice(index.getKernel(), index.getChannel());
   }

   public abstract T[] toArray();

   @Override
   public ObjectNDArray<T> transpose(@NonNull int... newAxes) {
      return Cast.as(super.transpose(newAxes));
   }

   @Override
   public ObjectNDArray<T> zero() {
      return fill(null);
   }

   @Override
   public ObjectNDArray<T> zeroLike() {
      return factory().zeros(shape());
   }

}//END OF ObjectNDArray
