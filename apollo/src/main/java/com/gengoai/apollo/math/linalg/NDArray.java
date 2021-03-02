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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.Copyable;
import com.gengoai.Primitives;
import com.gengoai.apollo.math.linalg.dense.*;
import com.gengoai.apollo.math.linalg.sparse.*;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.collection.Sorting;
import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;
import org.tensorflow.Tensor;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.gengoai.Validation.checkArgument;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p>An N-dimensional array type containing a collection of homogeneous items. </p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
      @JsonSubTypes.Type(value = DenseStringNDArray.class, name = "DSTRING"),
      @JsonSubTypes.Type(value = DenseFloat32NDArray.class, name = "DFLOAT32"),
      @JsonSubTypes.Type(value = DenseFloat64NDArray.class, name = "DFLOAT64"),
      @JsonSubTypes.Type(value = DenseInt32NDArray.class, name = "DINT32"),
      @JsonSubTypes.Type(value = DenseInt64NDArray.class, name = "DINT64"),

      @JsonSubTypes.Type(value = SparseStringNDArray.class, name = "SSTRING"),
      @JsonSubTypes.Type(value = SparseFloat32NDArray.class, name = "SFLOAT32"),
      @JsonSubTypes.Type(value = SparseFloat64NDArray.class, name = "SFLOAT64"),
      @JsonSubTypes.Type(value = SparseInt32NDArray.class, name = "SINT32"),
      @JsonSubTypes.Type(value = SparseInt64NDArray.class, name = "SINT64"),

})
@JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.NONE,
      setterVisibility = JsonAutoDetect.Visibility.NONE,
      getterVisibility = JsonAutoDetect.Visibility.NONE,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE,
      creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class NDArray implements Serializable, Observation {
   private static final NumberFormat decimalFormatter = new DecimalFormat(" 0.000000;-0");
   private static final String INVALID_AXIS = "Invalid axis %s for shape %s.";
   private static final String INVALID_DIMENSION = "Invalid dimension %d for axis %d (%d) of shape %s.";

   private static final long serialVersionUID = 1L;
   private static final char openP = '{';
   private static final char closeP = '}';
   @JsonProperty("shape")
   private final Shape shape;
   @JsonProperty("label")
   @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
   private Object label = null;
   @JsonProperty("predicted")
   @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
   private Object predicted = null;
   @JsonProperty("weight")
   private double weight = 1d;

   /**
    * Instantiates a new NDArray.
    *
    * @param shape the shape
    */
   public NDArray(Shape shape) {
      this.shape = shape.copy();
   }

   protected static int checkAxis(int axis, NDArray n) {
      int absAxis = n.shape().toAbsolute(axis);
      if (absAxis < (Shape.MAX_DIMENSIONS - n.rank()) || absAxis >= Shape.MAX_DIMENSIONS) {
         throw new IllegalArgumentException(String.format(INVALID_AXIS, absAxis, n.shape()));
      }
      return absAxis;
   }

   protected static void checkDimension(int axis, int dimension, NDArray n) {
      if (dimension < 0 || (dimension > 0 && dimension >= n.shape().get(axis))) {
         throw new IllegalArgumentException(String.format(INVALID_DIMENSION, dimension, axis, n.shape()
                                                                                               .toAbsolute(axis), n
                                                                .shape()));
      }
   }

   private static String formatElement(Object o, boolean isFloatingPoint) {
      if (isFloatingPoint) {
         return decimalFormatter.format(o);
      } else if (o instanceof String) {
         return Strings.abbreviate(o.toString(), 5);
      }
      return o == null ? "null" : o.toString();
   }

   private static void print(NDArray m, StringBuilder writer, int indent) {
      Class<?> c = Primitives.wrap(m.getType());
      String strIndent = Strings.repeat(' ', indent);
      boolean isFloatingPoint = c == Float.class || c == Double.class;


      if (m.shape().isEmpty()) {
         writer.append(openP).append(closeP);
         return;
      }

      if (m.shape().isScalar()) {
         writer.append(openP).append(m.scalar()).append(closeP);
         return;
      }

      if (m.shape().isVector() && m.shape().rows() <= 1) {
         writer.append(openP).append(formatElement(m.get(0), isFloatingPoint));
         for (int i = 1; i < m.length(); i++) {
            writer.append(", ")
                  .append(formatElement(m.get(i), isFloatingPoint));
         }
         writer.append(closeP);
         return;
      }


      if (m.shape().isMatrix()) {
         writer.append(openP);
         for (int row = 0; row < m.shape().rows(); row++) {
            if (row > 0) {
               writer.append(strIndent);
            }
            writer.append(openP);
            for (int column = 0; column < m.shape().columns(); column++) {
               if (column > 0) {
                  writer.append(", ");
               }
               writer.append(formatElement(m.get(row, column), isFloatingPoint));
            }
            writer.append(closeP);
            if (row + 1 < m.shape().rows()) {
               writer.append(",\n");
            }
         }
         writer.append(closeP);
         return;
      }

      if (m.rank() == 3) {
         writer.append(openP);
         for (int channel = 0; channel < m.shape().channels(); channel++) {
            if (channel > 0) {
               writer.append(strIndent);
            }
            print(m.slice(channel), writer, indent + 1);
            if (channel + 1 < m.shape().channels()) {
               writer.append(",\n");
            }
         }
         writer.append(closeP);
         return;
      }

      if (m.rank() == 4) {
         writer.append(openP);
         for (int kernel = 0; kernel < m.shape().kernels(); kernel++) {
            if (kernel > 0) {
               writer.append(strIndent);
            }
            print(m.slice(kernel, 0, kernel + 1, m.shape().channels()), writer, indent + 1);
            if (kernel + 1 < m.shape().kernels()) {
               writer.append(",\n");
            }
         }
         writer.append(closeP);
      }

   }

   /**
    * <p>Transposes the matrix on its diagonal switching the rows and columns.</p>
    *
    * @return the transposed array
    */
   public NDArray T() {
      if (isEmpty() || shape().isScalar()) {
         return copy();
      }
      NDArray out = factory().zeros(shape().with(Shape.ROW, Math.max(1, shape().columns()),
                                                 Shape.COLUMN, Math.max(1, shape().rows())));
      for (Index index : shape().range()) {
         Object value = get(index);
         int col = index.getColumn();
         int row = index.getRow();
         out.set(index.set(Shape.ROW, col).set(Shape.COLUMN, row), value);
      }
      return out;
   }


   /**
    * <p>Calculates the index in the NDArray with maximum value.</p>
    *
    * @return the index with maximum value
    */
   public Index argMax() {
      return optimum((a, b) -> Sorting.compare(a, b) > 0).v1;
   }

   /**
    * <p>Calculates the indices for the maximum values along the given axis</p>
    *
    * @param axis the axis to calculate the maximum over
    * @return the Integer-based NDArray having the indices of the maximum values in this NDArray for the given axis
    */
   public NumericNDArray argMax(int axis) {
      return optimum((a, b) -> Sorting.compare(a, b) > 0, axis).v1;
   }

   /**
    * <p>Calculates the index in the NDArray with maximum value.</p>
    *
    * @return the index with maximum value
    */
   public long argMaxOffset() {
      return shape.calculateOffset(optimum((a, b) -> Sorting.compare(a, b) > 0).v1);
   }

   /**
    * <p>Calculates the index in the NDArray with minimum value.</p>
    *
    * @return the index with minimum value
    */
   public Index argMin() {
      return optimum((a, b) -> Sorting.compare(a, b) < 0).v1;
   }

   /**
    * <p>Calculates the indices for the minimum values along the given axis</p>
    *
    * @param axis the axis to calculate the minimum over
    * @return the Integer-based NDArray having the indices of the minimum values in this NDArray for the given axis
    */
   public NumericNDArray argMin(int axis) {
      return optimum((a, b) -> Sorting.compare(a, b) < 0, axis).v1;
   }

   /**
    * <p>Calculates the index in the NDArray with minimum value.</p>
    *
    * @return the index with minimum value
    */
   public long argMinOffset() {
      return shape.calculateOffset(optimum((a, b) -> Sorting.compare(a, b) < 0).v1);
   }

   protected Object arrayForTensor() {
      if (shape.rank() == 0) {
         return Array.newInstance(Primitives.unwrap(getType()), (int) shape.length());
      } else if (shape.rank() == 1) {
         Object array = Array.newInstance(Primitives.unwrap(getType()), (int) shape.length());
         for (int i = 0; i < shape().length(); i++) {
            Array.set(array, i, get(i));
         }
         return array;
      } else if (shape.rank() == 2) {
         Object array = Array.newInstance(Primitives.unwrap(getType()), shape.rows(), shape.columns());
         shape().range().forEach(ii -> Array.set(Array.get(array, ii.getRow()), ii.getColumn(), get(ii)));
         return array;
      } else if (shape.rank() == 3) {
         Object array = Array
               .newInstance(Primitives.unwrap(getType()), shape.channels(), shape.rows(), shape.columns());
         shape().range().forEach(ii -> {
            Object channel = Array.get(array, ii.getChannel());
            Object row = Array.get(channel, ii.getRow());
            Array.set(row, ii.getColumn(), get(ii));
         });
         return array;
      }
      Object array = Array
            .newInstance(Primitives.unwrap(getType()), shape.kernels(), shape.channels(), shape.rows(), shape
                  .columns());
      shape().range().forEach(ii -> {
         Object kernel = Array.get(array, ii.getKernel());
         Object channel = Array.get(kernel, ii.getChannel());
         Object row = Array.get(channel, ii.getRow());
         Array.set(row, ii.getColumn(), get(ii));
      });
      return array;
   }

   @Override
   public NDArray asNDArray() {
      return this;
   }


   /**
    * <p>Casts this NDArray as an ObjectNDArray of the given data type.</p>
    *
    * @param <T>   the data type of the ObjectNDArray
    * @param dType the data type of the ObjectNDArray
    * @return the ObjectNDArray
    */
   public <T> ObjectNDArray<T> asObjectNDArray(@NonNull Class<T> dType) {
      throw new IllegalStateException("Cannot cast this NDArray of type '" +
                                            getType().getSimpleName()
                                            + "' as an ObjectNDArray of type '" +
                                            dType.getSimpleName() + "'");
   }

   /**
    * <p>Gets the number of channels in the NDArray</p>
    *
    * @return the number of channels in the NDArray
    */
   public int channels() {
      return shape.channels();
   }

   /**
    * <p>Gets the number of columns in the NDArray</p>
    *
    * @return the number of columns in the NDArray
    */
   public int columns() {
      return shape.columns();
   }

   /**
    * <p>Compacts the memory usages of sparse NDArrays.</p>
    *
    * @return this NDArray
    */
   public NDArray compact() {
      return this;
   }

   @Override
   public NDArray copy() {
      return Copyable.deepCopy(this);
   }


   /**
    * <p>Gets the {@link NDArrayFactory} associated with constructing this
    * NDArray</p>
    *
    * @return the NDArrayFactory
    */
   public NDArrayFactory factory() {
      return Cast.as(NDArrayFactory.forType(getType()));
   }

   /**
    * <p>Fills the NDArray with the given value</p>
    *
    * @param value the value to set all cells in the NDArray
    * @return This NDArray
    */
   public NDArray fill(Object value) {
      for (int i = 0; i < length(); i++) {
         set(i, value);
      }
      return this;
   }

   /**
    * <p>Gets the value associated for the given offset in the NDArray.</p>
    *
    * @param offset the offset
    * @return the value at the given offset
    */
   public Object get(long offset) {
      return get(shape.calculateIndex(offset));
   }

   /**
    * <p>Gets the value of the NDArray at the given row and column. (Assumes channel and kernel are 0)</p>
    *
    * @param row the row index
    * @param col the column index
    * @return the double value
    */
   public Object get(int row, int col) {
      return get(0, 0, row, col);
   }

   /**
    * <p>Gets the value of the NDArray at the given channel, row, and column (Assumes kernel is 0)</p>
    *
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @return the double value
    */
   public Object get(int channel, int row, int col) {
      return get(0, channel, row, col);
   }

   /**
    * <p>Gets the value of the NDArray at the given kernel, channel, row, and column</p>
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @return the double value
    */
   public abstract Object get(int kernel, int channel, int row, int col);

   /**
    * <p>Gets the value at the given index.</p>
    *
    * @param index the index
    * @return the t
    */
   public Object get(@NonNull Index index) {
      return get(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn());
   }

   /**
    * <p>Creates a new NDArray from the elements in the given IndexRange</p>
    *
    * @param range the range
    * @return the new NDArray
    */
   public NDArray get(@NonNull IndexRange range) {
      int[] r = new int[Coordinate.MAX_DIMENSIONS - 1];
      int size = 0;

      for (int i = Coordinate.MAX_DIMENSIONS - range.shape().rank(); i < Coordinate.MAX_DIMENSIONS - 1; i++) {
         if (range.shape().get(i) <= 1) {
            r[size] = i;
            size++;
         }
      }

      if (size == 0) {
         NDArray out = factory().zeros(range.shape());
         for (Index index : range) {
            Object value = get(index);
            index = index.subtract(range.lower());
            out.set(index, value);
         }
         return out;
      }

      int[] remove = Arrays.copyOfRange(r, 0, size);
      NDArray out = factory().zeros(range.shape().remove(remove));
      for (Index index : range) {
         Object value = get(index);
         index = index.subtract(range.lower());
         index = index.remove(remove);
         out.set(index, value);
      }
      return out;
   }

   /**
    * <p>Creates a copy of the given position of the given axis of this NDArray.</p>
    *
    * @param axis     the axis
    * @param position the position
    * @return the nd array
    */
   public NDArray getAxis(int axis, int position) {
      int[] remove = new int[]{axis};
      NDArray out = factory().zeros(shape().remove(remove));
      for (Index index : shape().iterateAlong(axis, position)) {
         out.set(index.remove(remove), get(index));
      }
      return out;
   }

   /**
    * <p>Creates a copy of the given position of the given axis of this NDArray.</p>
    *
    * @param axis      the axis
    * @param positions the position
    * @return the nd array
    */
   public NDArray getAxis(int axis, long[] positions) {
      NDArray out = factory().zeros(shape().with(axis, positions.length));
      int i = 0;
      for (long position : positions) {
         for (Index index : shape().iterateAlong(axis, (int) position)) {
            out.set(index.set(axis, i), get(index));
         }
         i++;
      }
      return out;
   }


   /**
    * <p>Gets the label associated with the NDArray</p>
    *
    * @param <V> the type of the label
    * @return the label
    */
   public <V> V getLabel() {
      return Cast.as(label);
   }

   /**
    * <p>Sets the label associated with the NDArray</p>
    *
    * @param label the label
    * @return This NDArray
    */
   public NDArray setLabel(Object label) {
      this.label = label;
      return this;
   }

   /**
    * <p>Gets the predicted label associated with this NDArray.</p>
    *
    * @param <V> the type parameter
    * @return the predicted label
    */
   public <V> V getPredicted() {
      return Cast.as(predicted);
   }

   /**
    * <p>Sets the predicted label for this NDArray.</p>
    *
    * @param predicted the predicted label
    * @return this NDArray
    */
   public NDArray setPredicted(Object predicted) {
      this.predicted = predicted;
      return this;
   }

   /**
    * <p>Gets the data type of the elements in this NDArray</p>
    *
    * @return the data type of the elements in this NDArray
    */
   public abstract Class<?> getType();

   @Override
   public Stream<Variable> getVariableSpace() {
      return Stream.empty();
   }

   /**
    * <p>Gets the weight associated with the NDArray.</p>
    *
    * @return the weight
    */
   public double getWeight() {
      return weight;
   }

   /**
    * <p>Sets the weight associated with the NDArray.</p>
    *
    * @param weight the weight
    * @return this NDArray
    */
   public NDArray setWeight(double weight) {
      this.weight = (float) weight;
      return this;
   }

   /**
    * <p>Checks if the NDArray is dense</p>
    *
    * @return True if the NDArray is dense, False otherwise
    */
   public abstract boolean isDense();

   /**
    * <p>Checks if the NDArray is empty (i.e. has a length of 0)</p>
    *
    * @return True if empty, False other wise
    */
   public boolean isEmpty() {
      return shape().isEmpty();
   }

   @Override
   public boolean isNDArray() {
      return true;
   }

   /**
    * <p>Checks whether or not this NDArray's elements are numeric</p>
    *
    * @return True - if the elements are numeric, False otherwise
    */
   public abstract boolean isNumeric();

   /**
    * <p>Gets the number of kernels in the NDArray</p>
    *
    * @return the number of kernels in the NDArray
    */
   public int kernels() {
      return shape.kernels();
   }

   /**
    * <p>The total number of elements in this NDArray.</p>
    *
    * @return the length (total number) of the elements in the NDArray
    */
   public long length() {
      if (isEmpty()) {
         return 0;
      }
      return shape().length();
   }

   @Override
   public void mapVariables(@NonNull Function<Variable, Variable> mapper) {
      throw new UnsupportedOperationException("NDArray does not support mapping.");
   }

   /**
    * <p>Calculates the maximum values along the given axis across all slices.</p>
    *
    * @param axis  the axis to calculate the maximum over
    * @param other the other
    * @return the maximum values in this NDArray for the given axis
    */
   public NDArray max(int axis, @NonNull int... other) {
      checkAxis(axis, this);
      for (int axe : other) {
         checkAxis(axe, this);
      }
      return optimum((a, b) -> Sorting.compare(a, b) > 0, axis, other).v2;
   }

   /**
    * <p>Calculates the maximum value in the NDArray across all slices.</p>
    *
    * @return the maximum value
    */
   public Object max() {
      return optimum((a, b) -> Sorting.compare(a, b) > 0).v2;
   }


   /**
    * <p>Calculates the  minimum values along the given axis across slices</p>
    *
    * @param axis  the axis to calculate the minimum over
    * @param other the other
    * @return the the minimum values in this NDArray for the given axis
    */
   public NDArray min(int axis, int... other) {
      return optimum((a, b) -> Sorting.compare(a, b) < 0, axis, other).v2;
   }

   /**
    * <p>Calculates the minimum value in the NDArray across all slices</p>
    *
    * @return the minimum value
    */
   public Object min() {
      return optimum((a, b) -> Sorting.compare(a, b) < 0).v2;
   }

   private Tuple2<Index, Object> optimum(BiFunction<Object, Object, Boolean> function) {
      Index pos = null;
      Object opt = null;
      for (Index index : shape().range()) {
         Object value = get(index);
         if (opt == null || function.apply(value, opt)) {
            pos = index;
            opt = value;
         }
      }
      return $(pos, opt);
   }

   private Tuple2<NumericNDArray, NDArray> optimum(BiFunction<Object, Object, Boolean> function, int axis, int... other) {
      if (shape().isEmpty()) {
         return $(nd.DINT32.empty(),
                  factory().empty());
      }
      if (shape().isScalar()) {
         return $(nd.DINT32.scalar(0),
                  factory().zeros(1).fill(scalar()));
      }
      checkAxis(axis, this);
      Shape s = shape().remove(axis, other);
      NDArray out = factory().zeros(s);
      NumericNDArray outPos = nd.DINT32.zeros(s).fill(-1);
      for (Index index : shape().range()) {
         Object value = get(index);
         Index outIndex = index.remove(axis, other);
         Object outValue = out.get(outIndex);
         if (outValue == null || outPos.getDouble(outIndex) < 0 || function.apply(value, outValue)) {
            out.set(outIndex, value);
            int optIndex = index.get(axis);
            outPos.set(outIndex, optIndex);
         }
      }

      return $(outPos, out);
   }

   /**
    * <p>Constructs a new NDArray where the given <code>axis</code> is changed to have <code>length</code> values,
    * where this NDArray will either be padded with zeros or nulls to extend its size to the new length or truncated to
    * match the new length. All padding is done at the end of the axis.</p>
    *
    * @param axis   the axis to pad.
    * @param length the new length of the given axis
    * @return the padded NDArray.
    */
   public abstract NDArray padPost(int axis, int length);

   /**
    * <p>Constructs a new NDArray where the given <code>axes</code> are changed to have the given <code>length</code>
    * values, where this NDArray will either be padded with zeros or nulls to extend its size to the new lengths or
    * truncated to match the new lengths. All padding is done at the end of the axis. Note that the argument to this
    * method expects (int, int) pairs where the first integer is the axis and the second the new length.</p>
    *
    * @param axisLengthPairs array of integers <code>axis1, length, axis2, length2, ... ,axisN, lengthN</code>
    * @return the padded NDArray.
    */
   public abstract NDArray padPost(@NonNull int... axisLengthPairs);

   /**
    * <p>Constructs a new NDArray where  where this NDArray will either be padded with zeros or nulls to extend its
    * size to the new length or truncated to the given Shape. All padding is done at the end of the axis.</p>
    *
    * @param paddedShape the shape of the padded NDArray
    * @return the padded NDArray.
    */
   public abstract NDArray padPost(@NonNull Shape paddedShape);

   /**
    * <p>Pretty Prints the NDArray</p>
    *
    * @return the string
    */
   public String pprint() {
      var writer = new StringBuilder();
      print(this, writer, 1);
      return writer.toString().strip();
   }

   /**
    * <p>Calculates the rank (number of axes) for the NDArray.</p>
    *
    * @return The rank of the tensor
    */
   public final int rank() {
      return shape.rank();
   }

   @Override
   public void removeVariables(@NonNull Predicate<Variable> filter) {
      throw new IllegalStateException("NDArray does not support removing variables");
   }

   /**
    * <p>Updates the shape of this NDArray. Note that the total number of elements cannot change.</p>
    *
    * @param newShape the new shape of the NDArray
    * @return this NDArray with new shape
    */
   public abstract NDArray reshape(@NonNull Shape newShape);

   /**
    * <p>Updates the shape of this NDArray. Note that the total number of elements cannot change.</p>
    *
    * @param dims the new dimensions of the NDArray
    * @return this NDArray with new shape
    */
   public NDArray reshape(@NonNull int... dims) {
      return reshape(Shape.shape(dims));
   }

   /**
    * <p>Gets the number of rows in the NDArray</p>
    *
    * @return the number of rows in the NDArray
    */
   public int rows() {
      return shape.rows();
   }

   /**
    * <p>Returns the scalar value of this NDArray (value at <code>(0,0,0,0)</code>)</p>
    *
    * @return the scalar value
    */
   public Object scalar() {
      return get(0);
   }


   /**
    * <p>Sets the value of the element at the given index. (row/column if vector, entry if other)</p>
    *
    * @param index the index
    * @param value the value
    * @return this NDArray
    */
   public NDArray set(long index, Object value) {
      return set(shape.calculateIndex(index), value);
   }

   /**
    * Sets the value of the element at the given row and column (assumes kernel and channel are 0).
    *
    * @param row   the row index
    * @param col   the column index
    * @param value the value
    * @return this NDArray
    */
   public NDArray set(int row, int col, Object value) {
      return set(0, 0, row, col, value);
   }

   /**
    * Sets the value of the element at the given row and column (assumes kernel and channel are 0).
    *
    * @param channel the channel
    * @param row     the row index
    * @param col     the column index
    * @param value   the value
    * @return this NDArray
    */
   public NDArray set(int channel, int row, int col, Object value) {
      return set(0, channel, row, col, value);
   }

   /**
    * Sets the value of the element at the given row and column (assumes kernel and channel are 0).
    *
    * @param kernel  the kernel
    * @param channel the channel
    * @param row     the row index
    * @param col     the column index
    * @param value   the value
    * @return this NDArray
    */
   public abstract NDArray set(int kernel, int channel, int row, int col, Object value);

   /**
    * <p>Sets the value of the element at the given index. (row/column if vector, entry if other)</p>
    *
    * @param index the index
    * @param value the value
    * @return this NDArray
    */
   public NDArray set(@NonNull Index index, Object value) {
      return set(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn(), value);
   }

   /**
    * <p>Sets the values along the given <code>axis</code> at the given <code>position</code> to those in the given
    * NDArray.</p>
    *
    * @param axis     the axis to set
    * @param position the position of the axis to set
    * @param rhs      the NDArray whose values we will copy
    * @return this NDArray
    */
   public NDArray setAxis(int axis, int position, @NonNull NDArray rhs) {
      return setRange(shape().iterateAlong(axis, position), rhs);
   }

   /**
    * <p>Sets the values along the given <code>axis</code> at the given <code>position</code> to the given value.</p>
    *
    * @param axis     the axis to set
    * @param position the position of the axis to set
    * @param rhs      the value to set the elements to
    * @return this NDArray
    */
   public NDArray setAxis(int axis, int position, Object rhs) {
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      for (Index index : shape().iterateAlong(axis, position)) {
         set(index, rhs);
      }
      return this;
   }

   /**
    * <p>Sets the range of values in this NDArray to those of the given <code>rhs</code>. The
    * given values NDArray will be broadcast as necessary.</p>
    *
    * @param indexRange The range of indices to use for setting the values from the given NDArray
    * @param rhs        the NDArray whose values we will assign to this NDArray.
    * @return this NDArray
    */
   public NDArray setRange(@NonNull IndexRange indexRange, @NonNull NDArray rhs) {
      for (Index index : indexRange) {
         if (rhs.shape().isVector()) {
            set(index, rhs.get(Math.max(index.getRow(), index.getColumn())));
         } else {
            set(index, rhs.get(rhs.shape().broadcast(index)));
         }
      }
      return this;
   }

   /**
    * <p>Sets the range of values in this NDArray to those of the given <code>rhs</code>. The
    * given values NDArray will be broadcast as necessary.</p>
    *
    * @param indexRange The range of indices to use for setting the values from the given NDArray
    * @param rhs        the value we will assign to this NDArray.
    * @return this NDArray
    */
   public NDArray setRange(@NonNull IndexRange indexRange, Object rhs) {
      indexRange.forEach(i -> set(i, rhs));
      return this;
   }


   /**
    * <p>Sets the slice at the given index.</p>
    *
    * @param index the slice index
    * @param slice the NDArray of values for the new slice
    * @return this NDArray
    */
   public NDArray setSlice(int index, @NonNull NDArray slice) {
      checkArgument(shape().matrixShape().equals(slice.shape().matrixShape()),
                    () -> "Cannot set slice of different shape " +
                          slice.shape() + " != " + shape());
      NDArray tSlice = slice(index);
      for (Index ii : tSlice.shape().matrixShape().range()) {
         tSlice.set(ii, slice.get(ii));
      }
      return this;
   }

   /**
    * <p>Sets the slice at the given index.</p>
    *
    * @param kernel  the kernel position
    * @param channel the channel position
    * @param slice   the NDArray of values for the new slice
    * @return this NDArray
    */
   public abstract NDArray setSlice(int kernel, int channel, @NonNull NDArray slice);

   /**
    * <p>Gets the shape of this NDArray.</p>
    *
    * @return the shape
    */
   public final Shape shape() {
      return shape;
   }

   /**
    * <p>Then number of sparse entries (dense NDArray will have <code>size()=length()</code>)</p>
    *
    * @return the number of sparse entries.
    */
   public long size() {
      return length();
   }

   /**
    * <p>Returns a view of a single slice of this NDArray. Note that changes to the slice will effect this NDArray.</p>
    *
    * @param index the slice index
    * @return the NDArray for the slice
    */
   public abstract NDArray slice(int index);

   /**
    * <p>Returns a view of a this NDArray made up of the slices ranging from the starting kernel and channel to the
    * ending kernel and channel. Note that changes to the slice will effect this NDArray.</p>
    *
    * @param startKernel  the start kernel
    * @param startChannel the start channel
    * @param endKernel    the end kernel
    * @param endChannel   the end channel
    * @return the sliced view of this NDArray
    */
   public abstract NDArray slice(int startKernel, int startChannel, int endKernel, int endChannel);

   /**
    * <p>Returns a  view of a single slice of this NDArray. Note that changes to the slice will effect this
    * NDArray.</p>
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @return the NDArray for the slice
    */
   public abstract NDArray slice(int kernel, int channel);

   /**
    * <p>Returns a  view of a single slice of this NDArray. Note that changes to the slice will effect this
    * NDArray.</p>
    *
    * @param index the slice index
    * @return the NDArray for the slice
    */
   public abstract NDArray slice(@NonNull Index index);


   /**
    * <p>Gets the indices of the sparse entries</p>
    *
    * @return the index array
    */
   public long[] sparseIndices() {
      return LongStream.range(0, length()).toArray();
   }


   protected Shape toSliceShape(int startKernel, int startChannel, int endKernel, int endChannel) {
      checkArgument(startKernel >= 0, () -> "Invalid starting kernel " + startKernel);
      checkArgument(startChannel >= 0, () -> "Invalid starting channel " + startChannel);
      checkArgument(startKernel < endKernel, () -> "Invalid kernel range [" + startKernel + ", " + endKernel + ")");
      checkArgument(startChannel < endChannel, () -> "Invalid channel range [" + startChannel + ", " + endChannel + ")");
      int nk = endKernel - startKernel;
      if (nk == 1) {
         nk = 0;
      }
      int nc = endChannel - startChannel;
      if (nk == 0 && nc == 1) {
         nc = 0;
      }
      return shape().with(Shape.KERNEL, nk,
                          Shape.CHANNEL, nc);
   }

   @Override
   public String toString() {
      StringBuilder out = new StringBuilder("array(");
      print(this, out, 7);
      out.append(", shape=")
         .append(shape())
         .append(", dType='")
         .append(getType().getSimpleName())
         .append("', weight=")
         .append(formatElement(weight, true).strip());
      if (label != null) {
         out.append(", label=").append(label);
      }
      if (predicted != null) {
         out.append(", predicted=").append(predicted);
      }
      out.append(")");
      return out.toString();
   }

   /**
    * <p>Creates a TensorFlow Tensor from this NDArray</p>
    *
    * @return the tensor
    */
   public Tensor<?> toTensor() {
      return Cast.as(Tensor.create(arrayForTensor()));
   }


   /**
    * <p>Transposes this NDArray where the argument to the method is the permutation of the axes in this NDArray., For
    * example: <code>transpose(3,2,1)</code> will transpose the COLUMN and CHANNEL axes.</p>
    *
    * @param newAxes the permuted axes of this NDArray
    * @return the transposed NDArray
    */
   public NDArray transpose(@NonNull int... newAxes) {
      if (shape().isEmpty()) {
         return factory().empty();
      }
      if (IntStream.of(newAxes).distinct().count() != newAxes.length) {
         throw new IllegalStateException("Repeated axis: " + Arrays.toString(newAxes));
      }
      if (newAxes.length != rank()) {
         throw new IllegalStateException("Invalid number of axes (" + newAxes.length + ") expecting " + shape().rank());
      }
      for (int i = 0; i < newAxes.length; i++) {
         newAxes[i] = shape().toAbsolute(newAxes[i]);
      }
      for (int a : newAxes) {
         if (a < Coordinate.MAX_DIMENSIONS - shape().rank()) {
            throw new IllegalStateException("Cannot transpose along " + a + "  with Shape " + shape());
         }
      }
      int[] n = {0, 1, 2, 3};
      System.arraycopy(newAxes, 0, n, n.length - newAxes.length, newAxes.length);
      NDArray out = factory().zeros(shape().get(n[0]), shape().get(n[1]), shape().get(n[2]), shape().get(n[3]));
      for (Index index : shape().range()) {
         out.set(index.get(n[0]), index.get(n[1]), index.get(n[2]), index.get(n[3]), get(index));
      }
      return out;
   }


   @Override
   public void updateVariables(@NonNull Consumer<Variable> updater) {
      throw new UnsupportedOperationException("NDArray does not support updating.");
   }

   /**
    * <p>Zeros out the entries of the NDArray</p>
    *
    * @return this NDArray filled with zero (or NULL) values
    */
   public abstract NDArray zero();

   /**
    * <p>Creates an NDArray of zeros with the same shape as this NDArray</p>
    *
    * @return the zero-valued NDArray
    */
   public abstract NDArray zeroLike();


   /**
    * <p>Interface for processing individual entries of an NDArray</p>
    *
    * @param <T> the type parameter
    */
   @FunctionalInterface
   public interface EntryConsumer<T> {

      /**
       * Consumes the value of the given index
       *
       * @param index the index
       * @param value the value
       */
      void apply(long index, T value);

   }
}//END OF NDArray

