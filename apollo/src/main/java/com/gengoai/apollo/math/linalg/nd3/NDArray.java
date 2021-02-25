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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.Copyable;
import com.gengoai.Primitives;
import com.gengoai.apollo.math.linalg.nd3.dense.DenseFloat32NDArray;
import com.gengoai.apollo.math.linalg.nd3.dense.DenseInt32NDArray;
import com.gengoai.apollo.math.linalg.nd3.dense.DenseInt64NDArray;
import com.gengoai.apollo.math.linalg.nd3.dense.DenseStringNDArray;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.model.sequence.SequenceValidator;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.collection.Sorting;
import com.gengoai.conversion.Cast;
import com.gengoai.math.Operator;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.tensorflow.Tensor;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.gengoai.Validation.checkArgument;
import static com.gengoai.apollo.math.linalg.nd3.Validator.*;
import static com.gengoai.tuple.Tuples.$;

/**
 * The type Nd array.
 *
 * @param <T> the type parameter
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
      @JsonSubTypes.Type(value = DenseStringNDArray.class, name = "DSTRING"),
      @JsonSubTypes.Type(value = DenseFloat32NDArray.class, name = "DFLOAT32"),
      @JsonSubTypes.Type(value = DenseInt32NDArray.class, name = "DINT32"),
      @JsonSubTypes.Type(value = DenseInt64NDArray.class, name = "DINT64"),
})
@JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.NONE,
      setterVisibility = JsonAutoDetect.Visibility.NONE,
      getterVisibility = JsonAutoDetect.Visibility.NONE,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE,
      creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class NDArray<T> implements Serializable, Observation {
   protected static final NumberFormat decimalFormatter = new DecimalFormat(" 0.000000;-0");
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
    * Instantiates a new Nd array.
    *
    * @param shape the shape
    */
   public NDArray(Shape shape) {
      this.shape = shape.copy();
   }

   private static String formatElement(Object o, boolean isFloatingPoint) {
      if (isFloatingPoint) {
         return decimalFormatter.format(o);
      } else if (o instanceof String) {
         return Strings.abbreviate(o.toString(), 5);
      }
      return o == null ? "null" : o.toString();
   }

   private static void print(NDArray<?> m, StringBuilder writer, int indent) {
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
   public NDArray<T> T() {
      if (isEmpty() || shape().isScalar()) {
         return copy();
      }
      NDArray<T> out = factory().zeros(shape().with(Shape.ROW, Math.max(1, shape().columns()),
                                                    Shape.COLUMN, Math.max(1, shape().rows())));
      for (Index index : shape().range()) {
         T value = get(index);
         int col = index.getColumn();
         int row = index.getRow();
         out.set(index.set(Shape.ROW, col).set(Shape.COLUMN, row), value);
      }
      return out;
   }


   /**
    * <p>Adds the values in this NDArray to those in the given NDArray returning a new NDArray with the output.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.add({1,1,1,2,2,2}); //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{   11,  21,  41,
    *       102, 202, 402}}
    * }
    * </pre>
    *
    * @param rhs the other NDArray whose values will be added
    * @return the new NDArray with the result of this + other
    */
   public NDArray<T> add(@NonNull NDArray<?> rhs) {
      return mapDouble(rhs, Operator::add);
   }

   /**
    * <p>Adds the given scalar value to each of the values in this NDArray returning a new NDArray with the output.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.add(1);
    *
    *   //Would result in b being
    *   {{   11,  21,  41,
    *       101, 201, 401}}
    * }
    * </pre>
    *
    * @param value the value to add
    * @return the new NDArray with the scalar value added
    */
   public NDArray<T> add(double value) {
      return mapDouble(value, Operator::add);
   }

   /**
    * <p>Adds the given NDArray along the given axis, e.g. <code>add(Shape.ROW, cVector)</code> would add
    * <code>cVector</code> to each row. (this will span all axes higher than row, e.g. channel and
    * kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.add(Shape.ROW, {10,20,30});
    *
    *   //Would result in b being
    *   b = {{ 11,22,33,
    *          14,25,36 },
    *        { 17,28,39,
    *           9,18,27 }}
    * }
    * </pre>
    *
    * @param axis the axis to add the given NDArray along
    * @param rhs  the NDArray to add
    * @return the resultant NDArray
    */
   public NDArray<T> add(int axis, @NonNull NDArray<?> rhs) {
      return mapAxisDouble(axis, rhs, Operator::add);
   }

   /**
    * <p>Adds the given NDArray along the given axis at the given dimension, e.g. <code>add(Shape.ROW, 1
    * cVector)</code> would add <code>cVector</code> to the row indexed at <code>1</code> (this will span all axes
    * higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.add(Shape.ROW, 0, {10,20,30});
    *
    *   //Would result in b being
    *   {{ 11,22,33,
    *       4, 5, 6 },
    *    { 17,28,39,
    *      -1,-2,-3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the value of the axis to perform the add on
    * @param rhs      the NDArray to add
    * @return the resultant NDArray
    */
   public NDArray<T> add(int axis, int position, @NonNull NDArray<?> rhs) {
      return mapAxisDouble(axis, position, rhs, Operator::add);
   }

   /**
    * <p>Adds the given NDArray along the given axis at the given position, e.g. <code>add(Shape.ROW, 1
    * cVector)</code> would add <code>cVector</code> to the row indexed at <code>1</code> (this will span all axes
    * higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.addi(Shape.ROW, 0, 10);
    *
    *   //Would result in b being
    *   {{ 11,12,13,
    *       4, 5, 6 },
    *    { 17,18,19,
    *      -1,-2,-3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the position to add
    * @return the resultant NDArray
    */
   public NDArray<T> add(int axis, int position, @NonNull Number rhs) {
      return mapAxisDouble(axis, position, rhs.doubleValue(), Operator::add);
   }

   /**
    * <p>Updates the value in this NDArray by adding to them the values in the given NDArray.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.addi({1,1,1,2,2,2}); //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{   11,  21,  41,
    *       102, 202, 402}}
    * }
    * </pre>
    *
    * @param rhs the other NDArray whose values will be added
    * @return this NDArray with the result of this + rhs
    */
   public NDArray<T> addi(@NonNull NDArray<?> rhs) {
      return mapiDouble(rhs, Operator::add);
   }

   /**
    * <p>Updates the value in this NDArray by adding to them the given value.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.addi(1);
    *
    *   //Would result in a being
    *   {{   11,  21,  41,
    *       101, 201, 401}}
    * }
    * </pre>
    *
    * @param value the value to add
    * @return this NDArray with the scalar value added
    */
   public NDArray<T> addi(double value) {
      return mapiDouble(value, Operator::add);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis by adding them to the values in the given NDArray,
    * e.g. <code>addi(Shape.ROW, cVector)</code> would add <code>cVector</code> to each row. (this will span all axes
    * higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{ 1, 2, 3,
    *           4, 5, 6 },
    *         { 7, 8, 9,
    *          -1,-2,-3 }}
    *
    *   //Performing
    *   a.addi(Shape.ROW, {10,20,30});
    *
    *   //Would result in a being
    *   {{ 11,22,33,
    *      14,25,36 },
    *    { 17,28,39,
    *       9,18,27 }}
    * }
    * </pre>
    *
    * @param axis the axis to add the given NDArray along
    * @param rhs  the NDArray to add
    * @return this NDArray with the results of the addition
    */
   public NDArray<T> addi(int axis, @NonNull NDArray<?> rhs) {
      return mapiAxisDouble(axis, rhs, Operator::add);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis for the given position by adding them to the values
    * in the given NDArray, e.g. <code>addi(Shape.ROW, 0, cVector)</code> would add <code>cVector</code> to each row at
    * index <code>0</code>. (this will span all axes higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.addi(Shape.ROW, 0, {10,20,30});
    *
    *   //Would result in a being
    *   {{ 11,22,33,
    *       4, 5, 6 },
    *    { 17,28,39,
    *      -1,-2,-3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the NDArray to add
    * @return this NDArray with the results of the addition
    */
   public NDArray<T> addi(int axis, int position, @NonNull NDArray<?> rhs) {
      return mapiAxisDouble(axis, position, rhs, Operator::add);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis for the given position by adding them to the values
    * in the given NDArray, e.g. <code>addi(Shape.ROW, 0, cVector)</code> would add <code>cVector</code> to each row at
    * index <code>0</code>. (this will span all axes higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.addi(Shape.ROW, 0, 10);
    *
    *   //Would result in a being
    *   {{ 11,12,13,
    *       4, 5, 6 },
    *    { 17,18,19,
    *      -1,-2,-3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the position to add
    * @return this NDArray with the results of the addition
    */
   public NDArray<T> addi(int axis, int position, @NonNull Number rhs) {
      return mapiAxisDouble(axis, position, rhs.doubleValue(), Operator::add);
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
   public NDArray<Integer> argMax(int axis) {
      return optimum((a, b) -> Sorting.compare(a, b) > 0, axis).v1;
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
   public NDArray<Integer> argMin(int axis) {
      return optimum((a, b) -> Sorting.compare(a, b) < 0, axis).v1;
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
   public com.gengoai.apollo.math.linalg.NDArray asNDArray() {
      return null;
   }

   /**
    * <p>Creates a new NDArray of the given <code>targetType</code> by converting the values in this array.</p>
    *
    * @param <V>        the type of the new NDArray
    * @param targetType the target data type
    * @return the new NDArray
    */
   public <V> NDArray<V> asType(@NonNull Class<? extends V> targetType) {
      Class<?> mt = Primitives.wrap(getType());
      Class<?> tt = Primitives.wrap(targetType);
      if (mt == tt) {
         return Cast.as(this);
      }
      return Cast.as(NDArrayFactory.forType(targetType).array(arrayForTensor()));
   }

   /**
    * <p>Compacts the memory usages of sparse NDArrays.</p>
    *
    * @return this NDArray
    */
   public NDArray<T> compact() {
      return this;
   }

   @Override
   public NDArray<T> copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Decode sequence sequence.
    *
    * @param encoder   the encoder
    * @param validator the validator
    * @return the sequence
    */
   public Sequence<?> decodeSequence(@NonNull Encoder encoder,
                                     @NonNull SequenceValidator validator) {
      checkThisIsNumeric(this);
      VariableSequence sequence = new VariableSequence();
      String previous = "O";
      for (int word = 0; word < shape().rows(); word++) {
         NDArray<?> matrix = getAxis(Shape.ROW, word);
         int l = matrix.shape().calculateMatrixIndex(matrix.argMax());
         String tag = encoder.decode(l);
         while (!validator.isValid(tag, previous, matrix)) {
            matrix.set(l, Double.NEGATIVE_INFINITY);
            l = matrix.shape().calculateMatrixIndex(matrix.argMax());
            tag = encoder.decode(l);
         }
         previous = tag;
         sequence.add(Variable.real(tag, matrix.getDouble(l)));
      }
      return sequence;
   }

   /**
    * <p>Divides the elements in this NDArray by those in the given NDArray returning a new NDArray with the output.
    * (Only supported by Numeric NDArray). The operation is applied with this NDArray's value as the first (i.e. left)
    * argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.div({1,1,1,2,2,2});
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{  10,  20,  40,
    *       50, 100, 200}}
    * }
    * </pre>
    *
    * @param rhs the NDArray to divide this NDArry by
    * @return the new NDArray with the result of this / other
    */
   public NDArray<T> div(@NonNull NDArray<?> rhs) {
      return mapDouble(rhs, Operator::divide);
   }

   /**
    * <p>Divides the elements in this NDArray by the given scalar value returning a new NDArray with the output. (Only
    * supported by Numeric NDArray) The operation is applied with this NDArray's value as the first (i.e. left)
    * argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.div(2);
    *
    *   //Would result in b being
    *   {{   5,  10,  20,
    *       50, 100, 200 }}
    * }
    * </pre>
    *
    * @param value the value to divide this NDArray by
    * @return the new NDArray with the scalar value divided
    */
   public NDArray<T> div(double value) {
      return mapDouble(value, Operator::divide);
   }

   /**
    * <p>Divides this NDArray by the given NDArray along the given axis, e.g. <code>div(Shape.ROW, cVector)</code>
    * would divide each row by <code>cVector</code> (this will span all dimensions higher than row, e.g. channel and
    * kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.div(Shape.ROW, {1,2,3});
    *
    *   //Would result in b being
    *   {{  1,   1, 1,
    *       4, 2.5, 3 },
    *    {  7, 4, 3,
    *      -1,-1,-1 }}
    * }
    * </pre>
    *
    * @param axis the axis to divide the given NDArray along
    * @param rhs  the NDArray to divide by
    * @return the resultant NDArray
    */
   public NDArray<T> div(int axis, @NonNull NDArray<?> rhs) {
      return mapAxisDouble(axis, rhs, Operator::divide);
   }

   /**
    * <p>Divides this NDArray by the given NDArray along the given axis at the given axisValue, e.g.
    * <code>div(Shape.ROW, 1 cVector)</code> would divide the row indexed at <code>1</code> by <code>cVector</code>
    * this will span all dimensions higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.div(Shape.ROW, 0, {1,2,3});
    *
    *   //Would result in b being
    *   {{  1, 1, 1,
    *       4, 5, 6 },
    *    {  7, 4, 3,
    *      -1,-2,-3 }}
    * }
    * </pre>
    *
    * @param axis      the axis to divide the given NDArray along
    * @param axisValue the axisValue of the axis to perform the divide on
    * @param rhs       the NDArray to divide by
    * @return the resultant NDArray
    */
   public NDArray<T> div(int axis, int axisValue, @NonNull NDArray<?> rhs) {
      return mapAxisDouble(axis, axisValue, rhs, Operator::divide);
   }

   /**
    * <p>Divides this NDArray by the given NDArray along the given axis at the given axisValue, e.g.
    * <code>div(Shape.ROW, 1 cVector)</code> would divide the row indexed at <code>1</code> by <code>cVector</code>
    * this will span all dimensions higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.div(Shape.ROW, 0, 2);
    *
    *   //Would result in b being
    *   {{  0.5, 1, 1.5,
    *       4, 5, 6 },
    *    {3.5, 4,4.5,
    *      -1,-2,-3 }}
    * }
    * </pre>
    *
    * @param axis      the axis to divide the given NDArray along
    * @param axisValue the axisValue of the axis to perform the divide on
    * @param rhs       the axisValue to divide by
    * @return the resultant NDArray
    */
   public NDArray<T> div(int axis, int axisValue, @NonNull Number rhs) {
      return mapAxisDouble(axis, axisValue, rhs.doubleValue(), Operator::divide);
   }

   /**
    * <p>Divides this NDArray by the given NDArray along the given axis, e.g. <code>div(Shape.ROW, cVector)</code>
    * would divide each row by <code>cVector</code> (this will span all dimensions higher than row, e.g. channel and
    * kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.divi(Shape.ROW, {1,2,3});
    *
    *   //Would result in a being
    *   {{  1,   1, 1,
    *       4, 2.5, 3 },
    *    {  7, 4, 3,
    *      -1,-1,-1 }}
    * }
    * </pre>
    *
    * @param axis the axis to divide the given NDArray along
    * @param rhs  the NDArray to divide by
    * @return this NDArray with the results of the division
    */
   public NDArray<T> divi(int axis, @NonNull NDArray<?> rhs) {
      return mapiAxisDouble(axis, rhs, Operator::divide);
   }

   /**
    * <p>Divides this NDArray by the given NDArray along the given axis at the given position, e.g.
    * <code>div(Shape.ROW, 1 cVector)</code> would divide the row indexed at <code>1</code> by <code>cVector</code>
    * this will span all dimensions higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.divi(Shape.ROW, 0, {1,2,3});
    *
    *   //Would result in a being
    *   {{  1, 1, 1,
    *       4, 5, 6 },
    *    {  7, 4, 3,
    *      -1,-2,-3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to divide the given NDArray along
    * @param position the position of the axis to perform the divide on
    * @param rhs      the NDArray to divide by
    * @return this NDArray with the results of the division
    */
   public NDArray<T> divi(int axis, int position, @NonNull NDArray<?> rhs) {
      return mapiAxisDouble(axis, position, rhs, Operator::divide);
   }

   /**
    * <p>Updates the value in this NDArray by dividing its values those in the given NDArray.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.divi({1,1,1,2,2,2}); //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{   10,  20,  40,
    *        50, 100, 200}}
    * }
    * </pre>
    *
    * @param rhs the other NDArray whose values will be divided
    * @return this NDArray with the result of <code>this / rhs</code>
    */
   public NDArray<T> divi(@NonNull NDArray<?> rhs) {
      return mapiDouble(rhs, Operator::divide);
   }

   /**
    * <p>Updates the position in this NDArray by dividing its values by the given position.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.divi(Shape.ROW, 0, 2);
    *
    *   //Would result in a being
    *   {{  0.5, 1, 1.5,
    *       4, 5, 6 },
    *    {3.5, 4,4.5,
    *      -1,-2,-3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to divide the given NDArray along
    * @param position the position of the axis to perform the divide on
    * @param rhs      the position to divide by
    * @return the resultant NDArray
    */
   public NDArray<T> divi(int axis, int position, @NonNull Number rhs) {
      return mapiAxisDouble(axis, position, rhs.doubleValue(), Operator::divide);
   }

   /**
    * <p>Updates the value in this NDArray by dividing it by the given value.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.divi(2);
    *
    *   //Would result in a being
    *   {{   5,  10,  20,
    *       50, 100, 200 }}
    * }
    * </pre>
    *
    * @param value the value to divide
    * @return this NDArray with the scalar value divided
    */
   public NDArray<T> divi(double value) {
      return mapiDouble(value, Operator::divide);
   }

   /**
    * <p>Calculates the dot product between this and the given other NDArray. (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *    // Let b be a (2,3) matrix.
    *    b = {{   2, 2, 1,
    *             1, 1, 2 }}
    *
    *   //Performing
    *   a.dot(b); // The rhs NDArray only needs to be the same total length and not the same shape.
    *
    *   //Would result in
    *    1200
    * }
    * </pre>
    *
    * @param rhs the NDArray to calculate the dot product with
    * @return NDArray of dot products
    */
   public double dot(@NonNull NDArray<?> rhs) {
      checkArgsAreNumeric(this, rhs);
      checkLengthMatch(rhs, this);
      double dot = 0;
      for (int i = 0; i < length(); i++) {
         dot += getDouble(i) * rhs.getDouble(i);
      }
      return dot;
   }

   /**
    * <p>Calculates the dot product between this and the given other NDArray along the given axis. (Only supported by
    * Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *    // Let b be a (1,3) vector.
    *    b = {  2, 2, 1 }
    *
    *   //Performing
    *   c = a.dot(b); // The rhs NDArray only needs to be the same total length and not the same shape.
    *
    *   //Would result in c being
    *    {{ 80,
    *       800 }}
    * }
    * </pre>
    *
    * @param rhs  the rhs
    * @param axis the axis
    * @return the nd array
    */
   public NDArray<T> dot(@NonNull NDArray<?> rhs, int axis, int... other) {
      checkArgsAreNumeric(this, rhs);
      if (isEmpty() && rhs.isEmpty()) {
         return factory().empty();
      }
      if (shape().isScalar() && rhs.shape().isScalar()) {
         return factory().zeros(1).set(0, scalarDouble() * rhs.scalarDouble());
      }
      Validator.checkAxis(axis, this);
      for (int i : other) {
         Validator.checkAxis(i, this);
      }
      Shape comp = shape().remove(axis, other);
      NDArray<T> out = factory().zeros(comp);
      for (Index index : shape().range()) {
         double lv = getDouble(index);
         double rv = rhs.getDouble(rhs.shape.broadcast(index));
         Index oIndex = index.remove(axis, other);
         out.set(oIndex, out.getDouble(oIndex) + (lv * rv));
      }
      return out;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj == this) {
         return true;
      }
      if (obj instanceof NDArray) {
         NDArray<?> r = Cast.as(obj);
         if (shape.equals(r.shape) && getType() == r.getType()) {
            return Arrays.equals(toFloatMatrix(), r.toFloatMatrix());
         }
      }
      return false;
   }

   /**
    * <p>Gets the {@link NDArrayFactory} associated with constructing this
    * NDArray</p>
    *
    * @return the NDArrayFactory
    */
   public NDArrayFactory<T> factory() {
      return Cast.as(NDArrayFactory.forType(getType()));
   }

   /**
    * <p>Fills the NDArray with the given value</p>
    *
    * @param value the value to set all cells in the NDArray
    * @return This NDArray
    */
   public NDArray<T> fill(T value) {
      for (int i = 0; i < length(); i++) {
         set(i, value);
      }
      return this;
   }

   /**
    * <p>Fills the NDArray with the given double value. (Only works on numeric NDArray).</p>
    *
    * @param value the value to set all cells in the NDArray
    * @return This NDArray
    */
   public NDArray<T> fill(double value) {
      checkThisIsNumeric(this);
      for (int i = 0; i < length(); i++) {
         set(i, value);
      }
      return this;
   }

   /**
    * <p>Fills the NDArray with the given value for elements which pass the given predicate.</p>
    *
    * @param test  the predicate to test values to determine if the corresponding element should be filled with the
    *              given value
    * @param value the value to set all cells in the NDArray
    * @return This NDArray
    */
   public NDArray<T> fillIf(@NonNull Predicate<? super T> test, T value) {
      for (int i = 0; i < length(); i++) {
         if (test.test(get(i))) {
            set(i, value);
         }
      }
      return this;
   }

   /**
    * <p>Fills the NDArray with the given value for elements which pass the given predicate.</p>
    *
    * @param test  the predicate to test values to determine if the corresponding element should be filled with the
    *              given value
    * @param value the value to set all cells in the NDArray
    * @return This NDArray
    */
   public NDArray<T> fillIf(@NonNull DoublePredicate test, double value) {
      checkThisIsNumeric(this);
      for (int i = 0; i < length(); i++) {
         if (test.test(getDouble(i))) {
            set(i, value);
         }
      }
      return this;
   }

   /**
    * Processes the sparse entries in this NDArray
    *
    * @param consumer the consumer
    */
   public void forEachSparse(@NonNull NDArray.EntryConsumer<T> consumer) {
      for (int i = 0; i < shape().length(); i++) {
         consumer.apply(i, get(i));
      }
   }

   /**
    * Get t.
    *
    * @param index the index
    * @return the t
    */
   public T get(long index) {
      return get(shape.calculateIndex(index));
   }

   /**
    * Gets the value of the NDArray at the given row and column. (Assumes channel and kernel are 0)
    *
    * @param row the row index
    * @param col the column index
    * @return the double value
    */
   public T get(int row, int col) {
      return get(0, 0, row, col);
   }

   /**
    * Gets the value of the NDArray at the given channel, row, and column (Assumes kernel is 0)
    *
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @return the double value
    */
   public T get(int channel, int row, int col) {
      return get(0, channel, row, col);
   }

   /**
    * Gets the value of the NDArray at the given kernel, channel, row, and column
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @return the double value
    */
   public abstract T get(int kernel, int channel, int row, int col);

   /**
    * Get t.
    *
    * @param index the index
    * @return the t
    */
   public T get(@NonNull Index index) {
      return get(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn());
   }

   /**
    * <p>Creates a new NDArray from the elements in the given IndexRange</p>
    *
    * @param range the range
    * @return the new NDArray
    */
   public NDArray<T> get(@NonNull IndexRange range) {
      int[] r = new int[Coordinate.MAX_DIMENSIONS - 1];
      int size = 0;

      for (int i = Coordinate.MAX_DIMENSIONS - range.shape().rank(); i < Coordinate.MAX_DIMENSIONS - 1; i++) {
         if (range.shape().get(i) <= 1) {
            r[size] = i;
            size++;
         }
      }

      if (size == 0) {
         NDArray<T> out = factory().zeros(range.shape());
         for (Index index : range) {
            T value = get(index);
            index = index.subtract(range.lower());
            out.set(index, value);
         }
         return out;
      }

      int[] remove = Arrays.copyOfRange(r, 0, size);
      NDArray<T> out = factory().zeros(range.shape().remove(remove));
      for (Index index : range) {
         T value = get(index);
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
   public NDArray<T> getAxis(int axis, int position) {
      int[] remove = new int[]{axis};
      NDArray<T> out = factory().zeros(shape().remove(remove));
      for (Index index : shape().iterateAlong(axis, position)) {
         out.set(index.remove(remove), get(index));
      }
      return out;
   }

   /**
    * Gets the value of the NDArray at the given row and column. (Assumes channel and kernel are 0)
    *
    * @param row the row index
    * @param col the column index
    * @return the double value
    */
   public double getDouble(int row, int col) {
      return getDouble(0, 0, row, col);
   }

   /**
    * Gets the value of the NDArray at the given channel, row, and column (Assumes kernel is 0)
    *
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @return the double value
    */
   public double getDouble(int channel, int row, int col) {
      return getDouble(0, channel, row, col);
   }

   /**
    * Gets the value of the NDArray at the given kernel, channel, row, and column
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @return the double value
    */
   public abstract double getDouble(int kernel, int channel, int row, int col);

   /**
    * <p>Gets the value at the given index as a double. (Only works on numeric NDArray)</p>
    *
    * @param index the index
    * @return the double value
    */
   public double getDouble(long index) {
      return getDouble(shape.calculateIndex(index));
   }

   /**
    * <p>Gets the value at the given index as a double. (Only works on numeric NDArray)</p>
    *
    * @param index the index
    * @return the double value
    */
   public double getDouble(@NonNull Index index) {
      return getDouble(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn());
   }

   /**
    * Gets the label associated with the NDArray
    *
    * @param <V> the type of the label
    * @return the label
    */
   public <V> V getLabel() {
      return Cast.as(label);
   }

   /**
    * Sets the label associated with the NDArray
    *
    * @param label the label
    * @return This NDArray
    */
   public NDArray<?> setLabel(Object label) {
      this.label = label;
      return this;
   }

   /**
    * Gets the predicted label associated with this NDArray.
    *
    * @param <V> the type parameter
    * @return the predicted label
    */
   public <V> V getPredicted() {
      return Cast.as(predicted);
   }

   /**
    * Sets the predicted label for this NDArray.
    *
    * @param predicted the predicted label
    * @return this NDArray
    */
   public NDArray<?> setPredicted(Object predicted) {
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
    * Gets the weight associated with the NDArray.
    *
    * @return the weight
    */
   public double getWeight() {
      return weight;
   }

   /**
    * Sets the weight associated with the NDArray.
    *
    * @param weight the weight
    * @return this NDArray
    */
   public NDArray<T> setWeight(double weight) {
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

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated using the given unary operator.</p>
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
    * }
    * </pre>
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> map(@NonNull UnaryOperator<T> operator) {
      NDArray<T> out = zeroLike();
      for (int i = 0; i < length(); i++) {
         out.set(i, operator.apply(get(i)));
      }
      return out;
   }

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated by the given binary operation with the given
    * value. The operation is applied with this NDArray's value as the first (i.e. left) argument.</p>
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
    * }
    * </pre>
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NDArray<T> map(T value, @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, value, operator, null);
   }

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated by the given binary operation with values in the
    * given rhs NDArray The operation is applied with this NDArray's value as the first (i.e. left) argument.</p>
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
    * }
    * </pre>
    *
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NDArray<T> map(@NonNull NDArray<? extends T> rhs, @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, rhs, operator, null);
   }

   /**
    * <p>Creates a new NDArray whose values are calculated using the given binary operator applied to the values of
    * this NDArray along the given <code>axis</code> with the values in the given NDArray. The operation is applied with
    * this NDArray's value as the first (i.e. left) argument.</p>
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
    * }
    * </pre>
    *
    * @param axis     the axis to map
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapAxis(int axis,
                             @NonNull NDArray<? extends T> rhs,
                             @NonNull BinaryOperator<T> operator) {
      checkAxis(axis, this);
      return NDArrayOps.map(this, axis, rhs, operator, null);
   }

   /**
    * <p>Updates the values along the given <code>dimension</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the given <code>value</code>. The operation is applied with this
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
    * }
    * </pre>
    *
    * @param axis     the axis to map
    * @param position the dimension of the axis to be updated
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapAxis(int axis,
                             int position,
                             T value,
                             @NonNull BinaryOperator<T> operator) {
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      return NDArrayOps.map(this, axis, position, value, operator, null);
   }

   /**
    * <p>Updates the values along the given <code>position</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the values in the given NDArray. The operation is applied with this
    * NDArray's value as the first (i.e. left) argument.</p>
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
    * }
    * </pre>
    *
    * @param axis     the axis to map
    * @param position the position of the axis to be updated
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapAxis(int axis,
                             int position,
                             NDArray<? extends T> rhs,
                             @NonNull BinaryOperator<T> operator) {
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      return NDArrayOps.map(this, axis, position, rhs, operator, null);
   }

   /**
    * <p>Updates the values along the given <code>dimension</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the given <code>value</code>. The operation is applied with this
    * NDArray's value as the first (i.e. left) argument.  (Only supported by numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.mapAxisDouble(Shape.ROW, {1, 2, 4}, Operator::divide);
    *   //Note the rhs NDArray only needs to have the correct length but not shape
    *
    *   //Would result in b being
    *   {{   10,  10,  10,
    *       100, 100, 100}}
    * }
    * </pre>
    *
    * @param axis     the axis to map
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapAxisDouble(int axis,
                                   @NonNull NDArray<?> rhs,
                                   @NonNull DoubleBinaryOperator operator) {
      checkThisIsNumeric(this);
      checkAxis(axis, this);
      return NDArrayOps.mapDouble(this, axis, rhs, operator, null);
   }

   /**
    * <p>Updates the values along the given <code>dimension</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the given <code>value</code>. The operation is applied with this
    * NDArray's value as the first (i.e. left) argument. (Only supported by numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.mapAxisDouble(Shape.ROW, 0, {1, 2, 4}, Operator::divide);
    *   //Note the rhs NDArray only needs to have the correct length but not shape
    *
    *   //Would result in b being
    *   {{   10,  10,  10,
    *       100, 200, 400}}
    * }
    * </pre>
    *
    * @param axis     the axis to map
    * @param position the dimension of the axis to be updated
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapAxisDouble(int axis,
                                   int position,
                                   @NonNull NDArray<?> rhs,
                                   @NonNull DoubleBinaryOperator operator) {
      checkArgsAreNumeric(this, rhs);
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      return NDArrayOps.mapDouble(this, axis, position, rhs, operator, null);
   }

   /**
    * <p>Updates the values along the given <code>dimension</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the given <code>doubleValue</code>. The operation is applied with this
    * NDArray's doubleValue as the first (i.e. left) argument. (Only supported by numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.mapAxisDouble(Shape.ROW, 0, 2, Operator::divide);
    *
    *   //Would result in b being
    *   {{    5,  10,  20,
    *       100, 200, 400}}
    * }
    * </pre>
    *
    * @param axis        the axis to map
    * @param position    the dimension of the axis to be updated
    * @param doubleValue the doubleValue
    * @param operator    the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapAxisDouble(int axis,
                                   int position,
                                   double doubleValue,
                                   @NonNull DoubleBinaryOperator operator) {
      checkThisIsNumeric(this);
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      return NDArrayOps.mapDouble(this, axis, position, doubleValue, operator, null);
   }

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated using the given unary operator.
    * (Only supported by numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.mapDouble(a -> a / 10);
    *
    *   //Would result in b being
    *   {{   1,  2,  4,
    *       10, 20, 40}}
    * }
    * </pre>
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapDouble(@NonNull DoubleUnaryOperator operator) {
      checkThisIsNumeric(this);
      NDArray<T> out = zeroLike();
      for (int i = 0; i < length(); i++) {
         out.set(i, operator.applyAsDouble(getDouble(i)));
      }
      return out;
   }

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated by the given binary operation with values in the
    * given rhs NDArray The operation is applied with this NDArray's value as the first (i.e. left) argument. (Only
    * supported by numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.mapDouble(a, Operator::divide);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{   1,  1,  1,
    *        1,  1, 1}}
    * }
    * </pre>
    *
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NDArray<T> mapDouble(@NonNull NDArray<?> rhs,
                               @NonNull DoubleBinaryOperator operator) {
      return NDArrayOps.mapDouble(this, rhs, operator, null);
   }

   /**
    * <p>Creates a new NDArray with values from this NDArray evaluated by the given binary operation with the given
    * value. The operation is applied with this NDArray's value as the first (i.e. left) argument. (Only supported by
    * numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.mapDouble(10, Operator::divide);
    *
    *   //Would result in b being
    *   {{   1,  2,  4,
    *       10, 20, 40}}
    * }
    * </pre>
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NDArray<T> mapDouble(double value,
                               @NonNull DoubleBinaryOperator operator) {
      return NDArrayOps.mapDouble(this, value, operator, null);
   }

   @Override
   public void mapVariables(@NonNull Function<Variable, Variable> mapper) {
      throw new UnsupportedOperationException("NDArray does not support mapping.");
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
    * }
    * </pre>
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapi(@NonNull UnaryOperator<T> operator) {
      for (int i = 0; i < length(); i++) {
         set(i, operator.apply(get(i)));
      }
      return this;
   }

   /**
    * <p>Updates the values in this NDArray by applying the given binary operation to each element with the given
    * value. The operation is applied with this NDArray's value as the first (i.e. left) argument.</p>
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
    * }
    * </pre>
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NDArray<T> mapi(T value, @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, value, operator, this);
   }

   /**
    * <p>Updates the values in this NDArray by applying the given binary operation to each element with the associated
    * element in the <code>rhs</code> NDArray. The operation is applied with this NDArray's value as the first (i.e.
    * left) argument.</p>
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
    * }
    * </pre>
    *
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NDArray<T> mapi(@NonNull NDArray<? extends T> rhs, @NonNull BinaryOperator<T> operator) {
      return NDArrayOps.map(this, rhs, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>axis</code> of this NDArray by performing the given binary operation
    * with the values in the given NDArray. The operation is applied with this NDArray's value as the first (i.e. left)
    * argument.</p>
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
    * }
    * </pre>
    *
    * @param axis     the axis to map
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapiAxis(int axis,
                              @NonNull NDArray<? extends T> rhs,
                              @NonNull BinaryOperator<T> operator) {
      checkAxis(axis, this);
      return NDArrayOps.map(this, axis, rhs, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>axis</code> at the given <code>position</code>  of this NDArray by
    * performing the given binary operation with the given value. The operation is applied with this NDArray's value as
    * the first (i.e. left) argument.</p>
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
    * }
    * </pre>
    *
    * @param axis     the axis to apply the operation on
    * @param position the axis position to apply the operation on
    * @param rhs      the right hand side value to use in the binary operation
    * @param operator the operation to perform on the values of this NDArray and the given rhs value
    * @return this NDArray
    */
   public NDArray<T> mapiAxis(int axis,
                              int position,
                              T rhs,
                              @NonNull BinaryOperator<T> operator) {
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      return NDArrayOps.map(this, axis, position, rhs, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>position</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the values in the given NDArray. The operation is applied with this
    * NDArray's value as the first (i.e. left) argument.</p>
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
    * }
    * </pre>
    *
    * @param axis     the axis to apply the operation on
    * @param position the axis position to apply the operation on
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return this NDArray
    */
   public NDArray<T> mapiAxis(int axis,
                              int position,
                              NDArray<? extends T> rhs,
                              @NonNull BinaryOperator<T> operator) {
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      return NDArrayOps.map(this, axis, position, rhs, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>dimension</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the values in the given NDArray. The operation is applied with this
    * NDArray's value as the first (i.e. left) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.mapiAxisDouble(Shape.ROW, {1, 2, 4}, Operator::divide);
    *   //Note the rhs NDArray only needs to have the correct length but not shape
    *
    *   //Would result in a being
    *   {{   10,  10,  10,
    *       100, 100, 100}}
    * }
    * </pre>
    *
    * @param axis     the axis to map
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapiAxisDouble(int axis,
                                    @NonNull NDArray<?> rhs,
                                    @NonNull DoubleBinaryOperator operator) {
      checkArgsAreNumeric(this, rhs);
      checkAxis(axis, this);
      return NDArrayOps.mapDouble(this, axis, rhs, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>position</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the given <code>value</code>. The operation is applied with this
    * NDArray's value as the first (i.e. left) argument.  (Only supported by numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *    a.mapiAxisDouble(Shape.ROW, 0, 2, Operator::divide);
    *
    *   //Would result in a being
    *   {{    5,  10,  20,
    *       100, 200, 400}}
    * }
    * </pre>
    *
    * @param axis     the row whose values we want to manipulate
    * @param position the position of the axis to be updated
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapiAxisDouble(int axis,
                                    int position,
                                    double value,
                                    @NonNull DoubleBinaryOperator operator) {
      checkThisIsNumeric(this);
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      return NDArrayOps.mapDouble(this, axis, position, value, operator, this);
   }

   /**
    * <p>Updates the values along the given <code>position</code> of the given <code>axis</code> of this NDArray by
    * performing the given binary operation with the given <code>value</code>. The operation is applied with this
    * NDArray's value as the first (i.e. left) argument.  (Only supported by numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.mapAxisDouble(Shape.ROW, 0, {1, 2, 4}, Operator::divide);
    *   //Note the rhs NDArray only needs to have the correct length but not shape
    *
    *   //Would result in a being
    *   {{   10,  10,  10,
    *       100, 200, 400}}
    * }
    * </pre>
    *
    * @param axis     the row whose values we want to manipulate
    * @param position the position of the axis to be updated
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapiAxisDouble(int axis,
                                    int position,
                                    @NonNull NDArray<?> rhs,
                                    @NonNull DoubleBinaryOperator operator) {
      checkArgsAreNumeric(this, rhs);
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      return NDArrayOps.mapDouble(this, axis, position, rhs, operator, this);
   }

   /**
    * <p>Updates the values in this NDArray evaluated using the given unary operator. (Only supported by numeric
    * NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.mapiDouble(a -> a / 10);
    *
    *   //Would result in a being
    *   {{   1,  2,  4,
    *       10, 20, 40}}
    * }
    * </pre>
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return this NDArray with the operator applied
    */
   public NDArray<T> mapiDouble(@NonNull DoubleUnaryOperator operator) {
      checkThisIsNumeric(this);
      for (int i = 0; i < length(); i++) {
         set(i, operator.applyAsDouble(getDouble(i)));
      }
      return this;
   }

   /**
    * <>Updates the values int this NDArray by performing the given binary operation with the values in the given
    * NDArray. The operation is applied with this NDArray's value as the first (i.e. left) argument.  (Only supported by
    * numeric NDArray).</>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.mapiDouble(a, Operator::divide);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{   1,  1,  1,
    *        1,  1, 1}}
    * }
    * </pre>
    *
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NDArray<T> mapiDouble(@NonNull NDArray<?> rhs, @NonNull DoubleBinaryOperator operator) {
      checkArgsAreNumeric(this, rhs);
      return NDArrayOps.mapDouble(this, rhs, operator, this);
   }

   /**
    * <p>Updates the values in this NDArray by performing he given binary operation with the given value. The operation
    * is applied with this NDArray's value as the first (i.e. left) argument.  (Only supported by numeric NDArray).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.mapiDouble(10, Operator::divide);
    *
    *   //Would result in a being
    *   {{   1,  2,  4,
    *       10, 20, 40}}
    * }
    * </pre>
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NDArray<T> mapiDouble(double value, @NonNull DoubleBinaryOperator operator) {
      checkThisIsNumeric(this);
      return NDArrayOps.mapDouble(this, value, operator, this);
   }

   protected NDArray<T> matrixMultiplicationImpl(NDArray<?> rhs) {
      checkArgument(shape().columns() == rhs.shape().rows(),
                    () -> "Cannot multiply NDArray of shape " + shape() + " by NDArray of shape " + rhs
                          .shape());
      NDArray<T> out = factory().zeros(Shape.shape(shape().rows(), rhs.shape().columns()));
      for (int row = 0; row < shape().rows(); row++) {
         for (int lhsColumn = 0; lhsColumn < shape().columns(); lhsColumn++) {
            for (int rhsColumn = 0; rhsColumn < rhs.shape().columns(); rhsColumn++) {
               out.set(row, rhsColumn,
                       out.getDouble(row, rhsColumn) +
                             getDouble(row, lhsColumn) * rhs.getDouble(lhsColumn, rhsColumn));
            }
         }
      }
      return out;
   }

   /**
    * <p>Calculates the maximum values along the given axis across all slices.</p>
    *
    * @param axis the axis to calculate the maximum over
    * @return the maximum values in this NDArray for the given axis
    */
   public NDArray<T> max(int axis, @NonNull int... other) {
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
   public T max() {
      return optimum((a, b) -> Sorting.compare(a, b) > 0).v2;
   }

   /**
    * <p>Calculates the mean value in the NDArray across all slices. (Only supported by numeric NDArray).</p>
    *
    * @return the mean value
    */
   public double mean() {
      checkThisIsNumeric(this);
      return sum() / length();
   }

   /**
    * <p>Calculates the mean values along the given axis across slices in the NDArray. (Only supported by numeric
    * NDArray).</p>
    *
    * @return the mean value
    */
   public NDArray<Float> mean(int axis, @NonNull int... other) {
      checkThisIsNumeric(this);
      return NDArrayOps.reduceDoubleAxis(this, nd.DFLOAT32, Operator::add, axis, other)
                       .divi(IntStream.concat(IntStream.of(axis), IntStream.of(other))
                                      .map(shape::get)
                                      .distinct()
                                      .reduce(1, Operator::multiply));
   }

   /**
    * <p>Calculates the  minimum values along the given axis across slices</p>
    *
    * @param axis the axis to calculate the minimum over
    * @return the the minimum values in this NDArray for the given axis
    */
   public NDArray<T> min(int axis, int... other) {
      return optimum((a, b) -> Sorting.compare(a, b) < 0, axis, other).v2;
   }

   /**
    * <p>Calculates the minimum value in the NDArray across all slices</p>
    *
    * @return the minimum value
    */
   public T min() {
      return optimum((a, b) -> Sorting.compare(a, b) < 0).v2;
   }

   /**
    * <p>Creates a new NDArray by multiplying the (matrix) slices of this NDArray with those in the given NDArray.
    * (Only supported by numeric NDArray).</p>
    *
    * @param rhs the NDArray to multiply
    * @return the resulting NDArray
    */
   public NDArray<T> mmul(@NonNull NDArray<?> rhs) {
      checkArgsAreNumeric(this, rhs);
      checkArgument(shape().columns() == rhs.shape().rows(),
                    () -> "Cannot multiply NDArray of shape " +
                          shape() +
                          " by NDArray of shape " +
                          rhs.shape());
      if (shape().isVector() || shape().isMatrix()) {
         return matrixMultiplicationImpl(rhs);
      }
      return NDArrayOps.mapSlice(shape().with(Shape.COLUMN, rhs.shape.columns()), this, rhs, NDArray::mmul);
   }

   /**
    * <p>Multiplies the values in this NDArray to those in the given NDArray returning a new NDArray with the output.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.mul({1,1,1,2,2,2}); //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{   10,  20,  40,
    *       200, 400, 800}}
    * }
    * </pre>
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return the new NDArray with the result of <code>this * rhs</code>
    */
   public NDArray<T> mul(@NonNull NDArray<?> rhs) {
      return mapDouble(rhs, Operator::multiply);
   }

   /**
    * <p>Multiplies the given scalar value to each of the values in this NDArray returning a new NDArray with the
    * output. (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.mul(2);
    *
    *   //Would result in b being
    *   {{   20,  40,  80,
    *       200, 400, 800}}
    * }
    * </pre>
    *
    * @param value the value to multiply
    * @return the new NDArray with the scalar value multiply
    */
   public NDArray<T> mul(double value) {
      return mapDouble(value, Operator::multiply);
   }

   /**
    * <p>Multiples the given NDArray along the given axis, e.g. <code>add(Shape.ROW, cVector)</code> would add
    * <code>cVector</code> to each row. (this will span all axes higher than row, e.g. channel and
    * kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.mul(Shape.ROW, {10,20,30});
    *
    *   //Would result in b being
    *   b = {{ 10, 20, 30,
    *          40,100,180 },
    *        { 70,160,270
    *         -10,-40,-90 }}
    * }
    * </pre>
    *
    * @param axis the axis to multiply the given NDArray along
    * @param rhs  the NDArray to multiply
    * @return the resultant NDArray
    */
   public NDArray<T> mul(int axis, @NonNull NDArray<?> rhs) {
      return mapAxisDouble(axis, rhs, Operator::multiply);
   }

   /**
    * <p>Multiplies the given NDArray along the given axis at the given axisValue, e.g. <code>add(Shape.ROW, 1
    * cVector)</code> would add <code>cVector</code> to the row indexed at <code>1</code> (this will span all axes
    * higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.mul(Shape.ROW, 0, {10,20,30});
    *
    *   //Would result in b being
    *   {{ 10, 60, 90
    *       4,  5,  6 },
    *    { 70,160,270
    *      -1, -2, -3 }}
    * }
    * </pre>
    *
    * @param axis      the axis to multiply the given NDArray along
    * @param axisValue the axisValue of the axis to perform the multiply on
    * @param rhs       the NDArray to multiply
    * @return the resultant NDArray
    */
   public NDArray<T> mul(int axis, int axisValue, @NonNull NDArray<?> rhs) {
      return mapAxisDouble(axis, axisValue, rhs, Operator::multiply);
   }

   /**
    * <p>Multiplies the given NDArray along the given axis at the given axisValue, e.g. <code>add(Shape.ROW, 1
    * cVector)</code> would add <code>cVector</code> to the row indexed at <code>1</code> (this will span all axes
    * higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.mul(Shape.ROW, 0, 10);
    *
    *   //Would result in b being
    *   {{ 10, 20, 30
    *       4,  5,  6 },
    *    { 70, 80, 90
    *      -1, -2, -3 }}
    * }
    * </pre>
    *
    * @param axis      the axis to multiply the given NDArray along
    * @param axisValue the axisValue of the axis to perform the multiply on
    * @param rhs       the NDArray to multiply
    * @return the resultant NDArray
    */
   public NDArray<T> mul(int axis, int axisValue, @NonNull Number rhs) {
      return mapAxisDouble(axis, axisValue, rhs.doubleValue(), Operator::multiply);
   }

   /**
    * <p>Updates the value in this NDArray by multiplying to them the values in the given NDArray.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.muli({1,1,1,2,2,2}); //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{   10,  20,  40,
    *       200, 400, 800}}
    * }
    * </pre>
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return this NDArray
    */
   public NDArray<T> muli(@NonNull NDArray<?> rhs) {
      return mapiDouble(rhs, Operator::multiply);
   }

   /**
    * <p>Updates the value in this NDArray by multiplying to them the given value.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.muli(2);
    *
    *   //Would result in a being
    *   {{   20,  40,  80,
    *       200, 400, 800}}
    * }
    * </pre>
    *
    * @param value the value to multiply
    * @return this NDArray
    */
   public NDArray<T> muli(double value) {
      return mapiDouble(value, Operator::multiply);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis by multiplying them to the values in the given
    * NDArray, e.g. <code>addi(Shape.ROW, cVector)</code> would add <code>cVector</code> to each row. (this will span
    * all axes higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.muli(Shape.ROW, {10,20,30});
    *
    *   //Would result in a being
    *   {{ 10, 20, 30,
    *      40,100,180 },
    *    { 70,160,270
    *     -10,-40,-90 }}
    * }
    * </pre>
    *
    * @param axis the axis to multiply the given NDArray along
    * @param rhs  the NDArray to multiply
    * @return this NDArray
    */
   public NDArray<T> muli(int axis, @NonNull NDArray<?> rhs) {
      return mapiAxisDouble(axis, rhs, Operator::multiply);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis for the given position by adding them to the values
    * in the given NDArray, e.g. <code>addi(Shape.ROW, 0, cVector)</code> would add <code>cVector</code> to each row at
    * index <code>0</code>. (this will span all axes higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.muli(Shape.ROW, 0, {10,20,30});
    *
    *   //Would result in a being
    *   {{ 10, 60, 90
    *       4,  5,  6 },
    *    { 70,160,270
    *      -1, -2, -3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the NDArray to add
    * @return this NDArray with the results of the addition
    */
   public NDArray<T> muli(int axis, int position, @NonNull NDArray<?> rhs) {
      return mapiAxisDouble(axis, position, rhs, Operator::multiply);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis for the given position by adding them to the values
    * in the given NDArray, e.g. <code>addi(Shape.ROW, 0, cVector)</code> would add <code>cVector</code> to each row at
    * index <code>0</code>. (this will span all axes higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.muli(Shape.ROW, 0, 10);
    *
    *   //Would result in a being
    *   {{ 10, 20, 30
    *       4,  5,  6 },
    *    { 70, 80, 90
    *      -1, -2, -3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the NDArray to add
    * @return this NDArray with the results of the addition
    */
   public NDArray<T> muli(int axis, int position, Number rhs) {
      return mapiAxisDouble(axis, position, rhs.doubleValue(), Operator::multiply);
   }

   /**
    * <p>Calculates the norm1 of the values along the given axis in the NDArray across slices. (Only supported by
    * numeric NDArray).</p>
    *
    * @return the L1 norm value
    */
   public NDArray<T> norm1(int axis, @NonNull int... other) {
      checkThisIsNumeric(this);
      if (rank() <= 1) {
         return NDArrayOps.reduceDoubleAxis(this, (a, b) -> a + Math.abs(b), axis, other);
      }
      int[] f = IntStream.concat(IntStream.of(axis), IntStream.of(other))
                         .distinct()
                         .sorted()
                         .toArray();
      NDArray<T> sum = mapDouble(Math::abs).sum(f[0]);
      if (f.length > 1) {
         axis = f[1];
         int[] o = f.length > 2 ? Arrays.copyOfRange(f, 2, f.length) : new int[0];
         return sum.max(axis, o);
      }
      return sum;
   }

   /**
    * <o>Calculates the L1 norm of the NDArray across slices. (Only support numeric NDArray)</o>
    *
    * @return the L1 norm of the NDArray
    */
   public double norm1() {
      checkThisIsNumeric(this);
      double l1 = 0;
      for (Index index : shape().range()) {
         l1 += Math.abs(getDouble(index));
      }
      return l1;
   }

   /**
    * <p>Calculates the L2 norm -- or Magnitude -- of the NDArray for the given axis. (Only support numeric
    * NDArray)</p>
    *
    * @return the L2 norm value
    */
   public NDArray<T> norm2(int axis, int... other) {
      checkThisIsNumeric(this);
      if (shape().rank() <= 1) {
         return NDArrayOps.reduceDoubleAxis(this, (a, b) -> a + b * b, axis, other).mapiDouble(Math::sqrt);
      }
      NDArray<T> n = mapDouble(a -> a * a);
      return NDArrayOps.reduceDoubleAxis(n, Operator::add, axis, other).mapiDouble(Math::sqrt);
   }

   /**
    * <p>Calculates the L2 norm -- or Magnitude -- of the NDArray across slices. (Only support numeric NDArray)</p>
    *
    * @return the L2 norm of the NDArray
    */
   public double norm2() {
      checkThisIsNumeric(this);
      double l2 = 0;
      for (Index index : shape().range()) {
         double v = getDouble(index);
         l2 += (v * v);
      }
      return Math.sqrt(l2);
   }

   protected Tuple2<Index, T> optimum(BiFunction<T, T, Boolean> function) {
      Index pos = null;
      T opt = null;
      for (Index index : shape().range()) {
         T value = get(index);
         if (opt == null || function.apply(value, opt)) {
            pos = index;
            opt = value;
         }
      }
      return $(pos, opt);
   }

   protected Tuple2<NDArray<Integer>, NDArray<T>> optimum(BiFunction<T, T, Boolean> function, int axis, int... other) {
      if (shape().isEmpty()) {
         return $(nd.DINT32.empty(),
                  factory().empty());
      }
      if (shape().isScalar()) {
         return $(nd.DINT32.scalar(0),
                  factory().scalar(scalar()));
      }
      checkAxis(axis, this);
      Shape s = shape().remove(axis, other);
      NDArray<T> out = factory().zeros(s);
      NDArray<Integer> outPos = nd.DINT32.zeros(s).fill(-1);
      for (Index index : shape().range()) {
         T value = get(index);
         Index outIndex = index.remove(axis, other);
         T outValue = out.get(outIndex);
         if (outValue == null || outPos.get(outIndex) < 0 || function.apply(value, outValue)) {
            out.set(outIndex, value);
            int optIndex = index.get(axis);
            outPos.set(outIndex, optIndex);
         }
      }

      return $(outPos, out);
   }

   public NDArray<T> padPost(int axis, int maxLength) {
      checkArgument(maxLength > 0, "max length must be > 0");
      int absAxis = checkAxis(axis, this);
      return padPost(shape().with(absAxis, Math.max(maxLength, shape().get(absAxis))));
   }

   public NDArray<T> padPost(int axis1, int axis1Max,
                             int axis2, int axis2Max) {
      checkArgument(axis1Max > 0, "max length must be > 0");
      checkArgument(axis2Max > 0, "max length must be > 0");
      int absAxis1 = checkAxis(axis1, this);
      int absAxis2 = checkAxis(axis2, this);
      return padPost(shape().with(absAxis1, Math.max(axis1Max, shape().get(absAxis1)),
                                  absAxis2, Math.max(axis2Max, shape().get(absAxis2))));
   }

   public NDArray<T> padPost(@NonNull Shape newShape) {
      NDArray<T> out = factory().zeros(newShape);
      for (Index index : shape().range()) {
         if (newShape.contains(index)) {
            out.set(index, get(index));
         }
      }
      return out;
   }

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
    * <p>Calculates the rank (number of axes) for the tensor.</p>
    *
    * @return The rank of the tensor
    */
   public final int rank() {
      return shape.rank();
   }

   /**
    * <p>Takes the values in the left hand NDArray and divides them by the elements in this NDArray returning a new
    * NDArray with the output. (Only supported by Numeric NDArray). The operation is applied with this NDArray's value
    * as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.rdiv({100,200,400,100,200,400});
    *   //Note the lhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{  10, 10, 10,
    *       1,   1,  1}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rdiv(@NonNull NDArray<?> lhs) {
      return mapDouble(lhs, (a, b) -> b / a);
   }

   /**
    * <p>Performs a division on each element, <code>e</code>. in this NDArray as <code>value / e </code> returning a
    * new NDArray with the results.  The operation is applied with this NDArray's value as the second (i.e. right)
    * argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.rdiv(10);
    *
    *   //Would result in b being
    *   {{   1,  2,  4,
    *       10, 20, 40 }}
    * }
    * </pre>
    *
    * @param lhs the left hand side value for division
    * @return the new resultant NDArray
    */
   public NDArray<T> rdiv(double lhs) {
      return mapDouble(lhs, (a, b) -> b / a);
   }

   /**
    * <p>Takes the values in the left hand NDArray and divides them by the elements in this NDArray along the given
    * axis returning a new NDArray with the output. (Only supported by Numeric NDArray). The operation is applied with
    * this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.rdiv(Shape.ROW, {100,200,400});
    *   //Note the lhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{  10, 10, 10,
    *       1,   1,  1}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rdiv(int axis, @NonNull NDArray<?> lhs) {
      return mapAxisDouble(axis, lhs, (a, b) -> b / a);
   }

   /**
    * <p>Takes the values in the left hand NDArray and divides them by the elements in this NDArray along the given
    * axis at the given position returning a new NDArray with the output. (Only supported by Numeric NDArray). The
    * operation is applied with this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.rdiv(Shape.ROW, 0, {1,2,4});
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{  10, 10, 10,
    *      100,200,400}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rdiv(int axis, int position, @NonNull NDArray<?> lhs) {
      return mapAxisDouble(axis, position, lhs, (a, b) -> b / a);
   }

   /**
    * <p>Takes the left hand value and divide it by the elements in this NDArray along the given
    * axis at the given position returning a new NDArray with the output. (Only supported by Numeric NDArray). The
    * operation is applied with this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.rdiv(Shape.ROW, 0, 10);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{   1,  1,  1,
    *       10, 20, 40}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rdiv(int axis, int position, @NonNull Number lhs) {
      return mapAxisDouble(axis, position, lhs.doubleValue(), (a, b) -> b / a);
   }

   /**
    * <p>Updates the values in this NDArray by taking the values in the left hand NDArray and dividing them by the
    * elements in this NDArray . (Only supported by Numeric NDArray). The operation is applied with this NDArray's value
    * as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rdivi({100,200,400,100,200,400});
    *   //Note the lhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{  10, 10, 10,
    *       1,   1,  1}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rdivi(@NonNull NDArray<?> lhs) {
      return mapiDouble(lhs, (a, b) -> b / a);
   }

   /**
    * <p>Updates this NDArray by performing a division on each element, <code>e</code>. in this NDArray as
    * <code>value / e </code>.  The operation is applied with this NDArray's value as the second (i.e. right)
    * argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rdivi(10);
    *
    *   //Would result in a being
    *   {{   1,  2,  4,
    *       10, 20, 40 }}
    * }
    * </pre>
    *
    * @param lhs the left hand side value for division
    * @return the new resultant NDArray
    */
   public NDArray<T> rdivi(double lhs) {
      return mapiDouble(lhs, (a, b) -> b / a);
   }

   /**
    * <p>Updates this NDArray by taking the values in the left hand NDArray and dividing them by the elements in this
    * NDArray along the given axis. (Only supported by Numeric NDArray). The operation is applied with this NDArray's
    * value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rdivi(Shape.ROW, {100,200,400});
    *   //Note the lhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{  10, 10, 10,
    *       1,   1,  1}}
    * }
    * </pre>
    *
    * @param axis the axis to perform the operation along
    * @param lhs  the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rdivi(int axis, @NonNull NDArray<?> lhs) {
      return mapiAxisDouble(axis, lhs, (a, b) -> b / a);
   }

   /**
    * <p>Updates the values in this NDArray by taking the values in the left hand NDArray and dividing them by the
    * elements in this NDArray along the given axis at the given position. (Only supported by Numeric NDArray). The
    * operation is applied with this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rdivi(Shape.ROW, 0, {100,200,400});
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{  10, 10, 10,
    *      100,200,400}}
    * }
    * </pre>
    *
    * @param axis     the axis to perform the operation along
    * @param position the position of the axis to restrict the operation on
    * @param lhs      the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rdivi(int axis, int position, @NonNull NDArray<?> lhs) {
      return mapiAxisDouble(axis, position, lhs, (a, b) -> b / a);
   }

   /**
    * <p>Updates the values in this NDArray by taking the left hand value and dividing it by the
    * elements in this NDArray along the given axis at the given position. (Only supported by Numeric NDArray). The
    * operation is applied with this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rdivi(Shape.ROW, 0, 10);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{  1, 2, 4,
    *      10,20,40}}
    * }
    * </pre>
    *
    * @param axis     the axis to perform the operation along
    * @param position the position of the axis to restrict the operation on
    * @param lhs      the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rdivi(int axis, int position, @NonNull Number lhs) {
      return mapiAxisDouble(axis, position, lhs.doubleValue(), (a, b) -> b / a);
   }

   @Override
   public void removeVariables(@NonNull Predicate<Variable> filter) {
      throw new IllegalStateException("NDArray does not support removing variables");
   }

   /**
    * <p>Updates the shape of this NDArray. Note that the total number of elements cannot
    * chansender@whiteemailsdelivery.comge.</p>
    *
    * @param newShape the new shape of the NDArray
    * @return this NDArray with new shape
    */
   public abstract NDArray<T> reshape(@NonNull Shape newShape);

   public NDArray<T> reshape(@NonNull int... dims) {
      return reshape(Shape.shape(dims));
   }

   /**
    * <p>Takes the values in the left hand NDArray and subtracts them by the elements in this NDArray returning a new
    * NDArray with the output. (Only supported by Numeric NDArray). The operation is applied with this NDArray's value
    * as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.rsub({11,22,44,101,202,404});
    *   //Note the lhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{  1, 2, 4,
    *       1, 2, 4 }}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rsub(@NonNull NDArray<?> lhs) {
      return mapDouble(lhs, (a, b) -> b - a);
   }

   /**
    * <p>Performs a subtraction on each element, <code>e</code>. in this NDArray as <code>value - e </code> returning a
    * new NDArray with the results.  The operation is applied with this NDArray's value as the second (i.e. right)
    * argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.rsub(10);
    *
    *   //Would result in b being
    *   {{   0,  10,  30,
    *       90, 190, 390 }}
    * }
    * </pre>
    *
    * @param lhs the left hand side value for subtraction
    * @return the new resultant NDArray
    */
   public NDArray<T> rsub(double lhs) {
      return mapDouble(lhs, (a, b) -> b - a);
   }

   /**
    * <p>Takes the values in the left hand NDArray and subtracts them by the elements in this NDArray along the given
    * axis returning a new NDArray with the output. (Only supported by Numeric NDArray). The operation is applied with
    * this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.rsub(Shape.ROW, {10,20,40});
    *   //Note the lhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{  0,   0,   0,
    *      90, 180, 360}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rsub(int axis, @NonNull NDArray<?> lhs) {
      return mapAxisDouble(axis, lhs, (a, b) -> b - a);
   }

   /**
    * <p>Takes the values in the left hand NDArray and subtracts them by the elements in this NDArray along the given
    * axis at the given position returning a new NDArray with the output. (Only supported by Numeric NDArray). The
    * operation is applied with this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.sub(Shape.ROW, 0, {10,20,40});
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{   0,  0,  0,
    *       90, 180, 360}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rsub(int axis, int position, @NonNull NDArray<?> lhs) {
      return mapAxisDouble(axis, position, lhs, (a, b) -> b - a);
   }

   /**
    * <p>Takes the left hand value and subtracts them by the elements in this NDArray along the given
    * axis at the given position returning a new NDArray with the output. (Only supported by Numeric NDArray). The
    * operation is applied with this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.sub(Shape.ROW, 0, 10);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{   0,   10,  20,
    *       100, 200, 400}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rsub(int axis, int position, @NonNull Number lhs) {
      return mapAxisDouble(axis, position, lhs.doubleValue(), (a, b) -> b - a);
   }

   /**
    * <p>Updates this NDArray by taking the values in the left hand NDArray and subtracting them by the elements in
    * this NDArray along the given axis. (Only supported by Numeric NDArray). The operation is applied with this
    * NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rsubi(Shape.ROW, {10,20,40});
    *   //Note the lhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{  0,   0,   0,
    *     -90,-180,-360}}
    * }
    * </pre>
    *
    * @param axis the axis to perform the operation along
    * @param lhs  the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rsubi(int axis, @NonNull NDArray<?> lhs) {
      return mapiAxisDouble(axis, lhs, (a, b) -> b - a);
   }

   /**
    * <p>Updates the values in this NDArray by taking the values in the left hand NDArray and subtracting them by the
    * elements in this NDArray along the given axis at the given position. (Only supported by Numeric NDArray). The
    * operation is applied with this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rsubi(Shape.ROW, 0, {10,20,40});
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{   0,  0,  0,
    *      100,200,400}}
    * }
    * </pre>
    *
    * @param axis     the axis to perform the operation along
    * @param position the position of the axis to restrict the operation on
    * @param lhs      the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rsubi(int axis, int position, @NonNull NDArray<?> lhs) {
      return mapiAxisDouble(axis, position, lhs, (a, b) -> b - a);
   }

   /**
    * <p>Updates the values in this NDArray by taking the left hand value and subtracting it by the
    * elements in this NDArray along the given axis at the given position. (Only supported by Numeric NDArray). The
    * operation is applied with this NDArray's value as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rsubi(Shape.ROW, 0, 10);
    *   //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{   0, 10, 30,
    *      100,200,400}}
    * }
    * </pre>
    *
    * @param axis     the axis to perform the operation along
    * @param position the position of the axis to restrict the operation on
    * @param lhs      the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rsubi(int axis, int position, @NonNull Number lhs) {
      return mapiAxisDouble(axis, position, lhs.doubleValue(), (a, b) -> b - a);
   }

   /**
    * <p>Updates the values in this NDArray by taking the values in the left hand NDArray and subtracting them by the
    * elements in this NDArray . (Only supported by Numeric NDArray). The operation is applied with this NDArray's value
    * as the second (i.e. right) argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rsubi({20,30,50,101,201,401});
    *   //Note the lhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{  10, 10, 10,
    *       1,   1,  1}}
    * }
    * </pre>
    *
    * @param lhs the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NDArray<T> rsubi(@NonNull NDArray<?> lhs) {
      return mapiDouble(lhs, (a, b) -> b - a);
   }

   /**
    * <p>Updates this NDArray by performing a division on each element, <code>e</code>. in this NDArray as
    * <code>value / e </code>.  The operation is applied with this NDArray's value as the second (i.e. right)
    * argument.</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.rsubi(10);
    *
    *   //Would result in a being
    *   {{   0,  10,  30,
    *       90, 190, 390}}
    * }
    * </pre>
    *
    * @param lhs the left hand side value for division
    * @return the new resultant NDArray
    */
   public NDArray<T> rsubi(double lhs) {
      return mapiDouble(lhs, (a, b) -> b - a);
   }

   /**
    * <p>Returns the scalar value of this NDArray (value at <code>(0,0,0,0)</code>)</p>
    *
    * @return the scalar value
    */
   public T scalar() {
      return get(0);
   }

   /**
    * <p>Returns the scalar value of this NDArray (value at <code>(0,0,0,0)</code>)</p>
    *
    * @return the scalar value
    */
   public double scalarDouble() {
      checkThisIsNumeric(this);
      return getDouble(0);
   }

   /**
    * <p>Sets the value of the element at the given index. (row/column if vector, entry if other)</p>
    *
    * @param index the index
    * @param value the value
    * @return this NDArray
    */
   public NDArray<T> set(long index, T value) {
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
   public NDArray<T> set(int row, int col, T value) {
      return set(0, 0, row, col, value);
   }

   /**
    * Sets the value of the element at the given row and column (assumes kernel and channel are 0).
    *
    * @param row   the row index
    * @param col   the column index
    * @param value the value
    * @return this NDArray
    */
   public NDArray<T> set(int channel, int row, int col, T value) {
      return set(0, channel, row, col, value);
   }

   /**
    * Sets the value of the element at the given row and column (assumes kernel and channel are 0).
    *
    * @param row   the row index
    * @param col   the column index
    * @param value the value
    * @return this NDArray
    */
   public abstract NDArray<T> set(int kernel, int channel, int row, int col, T value);

   /**
    * <p>Sets the value of the element at the given index. (row/column if vector, entry if other)</p>
    *
    * @param index the index
    * @param value the value
    * @return this NDArray
    */
   public NDArray<T> set(@NonNull Index index, T value) {
      return set(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn(), value);
   }

   /**
    * <p>Sets the value of the element at the given index. (row/column if vector, entry if other)</p>
    *
    * @param index the index
    * @param value the value
    * @return this NDArray
    */
   public NDArray<T> set(long index, double value) {
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
   public NDArray<T> set(int row, int col, double value) {
      return set(0, 0, row, col, value);
   }

   /**
    * Sets the value of the element at the given row and column (assumes kernel and channel are 0).
    *
    * @param row   the row index
    * @param col   the column index
    * @param value the value
    * @return this NDArray
    */
   public NDArray<T> set(int channel, int row, int col, double value) {
      return set(0, channel, row, col, value);
   }

   /**
    * Sets the value of the element at the given row and column (assumes kernel and channel are 0).
    *
    * @param row   the row index
    * @param col   the column index
    * @param value the value
    * @return this NDArray
    */
   public abstract NDArray<T> set(int kernel, int channel, int row, int col, double value);

   /**
    * <p>Sets the value of the element at the given index. (row/column if vector, entry if other)</p>
    *
    * @param index the index
    * @param value the value
    * @return this NDArray
    */
   public NDArray<T> set(@NonNull Index index, double value) {
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
   public NDArray<T> setAxis(int axis, int position, @NonNull NDArray<? extends T> rhs) {
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
   public NDArray<T> setAxis(int axis, int position, @NonNull T rhs) {
      checkAxis(axis, this);
      checkDimension(axis, position, this);
      for (Index index : shape().iterateAlong(axis, position)) {
         set(index, rhs);
      }
      return this;
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
   public NDArray<T> setAxisDouble(int axis, int position, @NonNull NDArray<?> rhs) {
      return setRangeDouble(shape().iterateAlong(axis, position), rhs);
   }

   /**
    * <p>Sets the values along the given <code>axis</code> at the given <code>position</code> to the given value.</p>
    *
    * @param axis     the axis to set
    * @param position the position of the axis to set
    * @param rhs      the value to set the elements to
    * @return this NDArray
    */
   public NDArray<T> setAxisDouble(int axis, int position, double rhs) {
      return setRangeDouble(shape().iterateAlong(axis, position), rhs);
   }

   /**
    * <p>Sets the range of values in this NDArray to those of the given <code>rhs</code>. The
    * given values NDArray will be broadcast as necessary.</p>
    *
    * @param indexRange The range of indices to use for setting the values from the given NDArray
    * @param rhs        the NDArray whose values we will assign to this NDArray.
    * @return this NDArray
    */
   public NDArray<T> setRange(@NonNull IndexRange indexRange, @NonNull NDArray<? extends T> rhs) {
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
   public NDArray<T> setRange(@NonNull IndexRange indexRange, @NonNull T rhs) {
      indexRange.forEach(i -> set(i, rhs));
      return this;
   }

   /**
    * <p>Sets the range <code>[from, to)</code> of values in this NDArray to those of the given <code>rhs</code>. The
    * given values NDArray will be broadcast as necessary.</p>
    *
    * @param indexRange The range of indices to use for setting the values from the given NDArray
    * @param rhs        the NDArray whose values we will assign to this NDArray.
    * @return this NDArray
    */
   public NDArray<T> setRangeDouble(@NonNull IndexRange indexRange,
                                    @NonNull NDArray<?> rhs) {
      for (Index index : indexRange) {
         if (rhs.shape().isVector()) {
            set(index, rhs.getDouble(Math.max(index.getRow(), index.getColumn())));
         } else {
            set(index, rhs.getDouble(rhs.shape().broadcast(index)));
         }
      }
      return this;
   }

   /**
    * <p>Sets the range <code>[from, to)</code> of values in this NDArray to those of the given <code>rhs</code>. The
    * given values NDArray will be broadcast as necessary.</p>
    *
    * @param indexRange The range of indices to use for setting the values from the given NDArray
    * @param rhs        the NDArray whose values we will assign to this NDArray.
    * @return this NDArray
    */
   public NDArray<T> setRangeDouble(@NonNull IndexRange indexRange, double rhs) {
      indexRange.forEach(i -> set(i, rhs));
      return this;
   }

   /**
    * Sets the slice at the given index.
    *
    * @param index the slice index
    * @param slice the NDArray of values for the new slice
    * @return this NDArray
    */
   public NDArray<T> setSlice(int index, @NonNull NDArray<T> slice) {
      checkArgument(shape().matrixShape().equals(slice.shape().matrixShape()),
                    () -> "Cannot set slice of different shape " +
                          slice.shape() + " != " + shape());
      NDArray<T> tSlice = slice(index);
      for (Index ii : tSlice.shape().matrixShape().range()) {
         tSlice.set(ii, slice.get(ii));
      }
      return this;
   }

   /**
    * Sets the slice at the given index.
    *
    * @param kernel  the kernel position
    * @param channel the channel position
    * @param slice   the NDArray of values for the new slice
    * @return this NDArray
    */
   public NDArray<T> setSlice(int kernel, int channel, @NonNull NDArray<T> slice) {
      return setSlice(shape().calculateSliceIndex(kernel, channel), slice);
   }

   /**
    * Gets the shape of this NDArray.
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
    * Returns a  view of a single slice of this NDArray. Note that changes to the slice will effect this NDArray.
    *
    * @param index the slice index
    * @return the NDArray for the slice
    */
   public abstract NDArray<T> slice(int index);

   public abstract NDArray<T> slice(int startKernel, int startChannel, int endKernel, int endChannel);

   /**
    * Returns a  view of a single slice of this NDArray. Note that changes to the slice will effect this NDArray.
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @return the NDArray for the slice
    */
   public NDArray<T> slice(int kernel, int channel) {
      return slice(shape.calculateSliceIndex(kernel, channel));
   }

   /**
    * Returns a  view of a single slice of this NDArray. Note that changes to the slice will effect this NDArray.
    *
    * @param index the slice index
    * @return the NDArray for the slice
    */
   public NDArray<T> slice(@NonNull Index index) {
      return slice(index.getKernel(), index.getChannel());
   }

   /**
    * Gets the indices of the sparse entries
    *
    * @return the index array
    */
   public long[] sparseIndices() {
      return LongStream.range(0, length()).toArray();
   }

   /**
    * <p>Subtracts the values in this NDArray to those in the given NDArray returning a new NDArray with the output.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.sub({1,1,1,2,2,2}); //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in b being
    *   {{    9,  19,  29,
    *        98, 198, 398}}
    * }
    * </pre>
    *
    * @param rhs the other NDArray whose values will be subtract
    * @return the new NDArray with the result of <code>this - other</code>
    */
   public NDArray<T> sub(@NonNull NDArray<?> rhs) {
      return mapDouble(rhs, Operator::subtract);
   }

   /**
    * <p>Subtracts the given scalar value to each of the values in this NDArray returning a new NDArray with the
    * output. (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   b = a.sub(1);
    *
    *   //Would result in b being
    *   {{   9,  19,  39,
    *       99, 199, 399}}
    * }
    * </pre>
    *
    * @param value the value to subtracted
    * @return the new NDArray with the scalar value subtracted
    */
   public NDArray<T> sub(double value) {
      return mapDouble(value, Operator::subtract);
   }

   /**
    * <p>Subtracts the given NDArray along the given axis, e.g. <code>sub(Shape.ROW, cVector)</code> would add
    * <code>cVector</code> to each row. (this will span all axes higher than row, e.g. channel and
    * kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.sub(Shape.ROW, {1,2,3});
    *
    *   //Would result in b being
    *   b = {{ 0,  0,  0
    *          3,  3,  3 },
    *        { 6,  6,  6,
    *          0,  0,  0 }}
    * }
    * </pre>
    *
    * @param axis the axis to subtract the given NDArray along
    * @param rhs  the NDArray to subtract
    * @return the resultant NDArray
    */
   public NDArray<T> sub(int axis, @NonNull NDArray<?> rhs) {
      return mapAxisDouble(axis, rhs, Operator::subtract);
   }

   /**
    * <p>Subtracts the given NDArray along the given axis at the given position, e.g. <code>sub(Shape.ROW, 1
    * cVector)</code> would add <code>cVector</code> to the row indexed at <code>1</code> (this will span all axes
    * higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.add(Shape.ROW, 0, {1,2,3});
    *
    *   //Would result in b being
    *   {{  0,  0,  0,
    *       4,  5,  6 },
    *    {  6,  6,  6,
    *      -1, -2, -3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to subtract the given NDArray along
    * @param position the position of the axis to perform the subtract on
    * @param rhs      the NDArray to subtract
    * @return the resultant NDArray
    */
   public NDArray<T> sub(int axis, int position, @NonNull NDArray<?> rhs) {
      return mapAxisDouble(axis, position, rhs, Operator::subtract);
   }

   /**
    * <p>Subtracts the given NDArray along the given axis at the given position, e.g. <code>sub(Shape.ROW, 1
    * 10)</code> would subtract <code>10</code> to the row indexed at <code>1</code> (this will span all axes higher
    * than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   b = a.add(Shape.ROW, 0,  2});
    *
    *   //Would result in b being
    *   {{ -1,  0,  1,
    *       4,  5,  6 },
    *    {  5,  6,  7,
    *      -1, -2, -3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to subtract the given NDArray along
    * @param position the position of the axis to perform the subtract on
    * @param rhs      the number to subtract
    * @return the resultant NDArray
    */
   public NDArray<T> sub(int axis, int position, @NonNull Number rhs) {
      return mapAxisDouble(axis, position, rhs.doubleValue(), Operator::subtract);
   }

   /**
    * <p>Updates the value in this NDArray by subtracting to them the values in the given NDArray.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.subi({1,1,1,2,2,2}); //Note the rhs NDArray only needs to have the same length not the same shape
    *
    *   //Would result in a being
    *   {{    9,  19,  29,
    *        98, 198, 398}}
    * }
    * </pre>
    *
    * @param rhs the other NDArray whose values will be subtracted
    * @return this NDArray with the result of <code>this - rhs</code>
    */
   public NDArray<T> subi(@NonNull NDArray<?> rhs) {
      return mapiDouble(rhs, Operator::subtract);
   }

   /**
    * <p>Updates the value in this NDArray by subtracting from them the given value.
    * (Only supported by Numeric NDArray)</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,3) matrix.
    *    a = {{   10,  20,  40,
    *            100, 200, 400 }}
    *
    *   //Performing
    *   a.subi(1);
    *
    *   //Would result in a being
    *   {{   9,  19,  39,
    *       99, 199, 399}}
    * }
    * </pre>
    *
    * @param value the value to subtract
    * @return this NDArray with the scalar value subtracted
    */
   public NDArray<T> subi(double value) {
      return mapiDouble(value, Operator::subtract);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis by subtracting from them the values in the given
    * NDArray, e.g. <code>subi(Shape.ROW, cVector)</code> would add <code>cVector</code> to each row. (this will span
    * all axes higher than row, e.g. channel and kernel in the case of row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.subi(Shape.ROW, {1,2,3});
    *
    *   //Would result in a being
    *   {{ 0,  0,  0
    *          3,  3,  3 },
    *        { 6,  6,  6,
    *          0,  0,  0 }}
    * }
    * </pre>
    *
    * @param axis the axis to subtract the given NDArray along
    * @param rhs  the NDArray to subtract
    * @return this NDArray with the results of the subtraction
    */
   public NDArray<T> subi(int axis, @NonNull NDArray<?> rhs) {
      return mapiAxisDouble(axis, rhs, Operator::subtract);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis for the given position by subtracting from them the
    * values in the given NDArray, e.g. <code>subi(Shape.ROW, 0, cVector)</code> would add <code>cVector</code> to each
    * row at index <code>0</code>. (this will span all axes higher than row, e.g. channel and kernel in the case of
    * row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.add(Shape.ROW, 0, {1,2,3});
    *
    *   //Would result in a being
    *   {{  0,  0,  0,
    *       4,  5,  6 },
    *    {  6,  6,  6,
    *      -1, -2, -3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to subtract the given NDArray along
    * @param position the position of the axis to perform the subtract on
    * @param rhs      the NDArray to subtract
    * @return this NDArray with the results of the subtraction
    */
   public NDArray<T> subi(int axis, int position, @NonNull NDArray<?> rhs) {
      return mapiAxisDouble(axis, position, rhs, Operator::subtract);
   }

   /**
    * <p>Updates the values in this NDArray along the given axis for the given position by subtracting from them the
    * values in the given NDArray, e.g. <code>subi(Shape.ROW, 0, 10)</code> would subtract <code>10</code> from each row
    * at index <code>0</code>. (this will span all axes higher than row, e.g. channel and kernel in the case of
    * row).</p>
    *
    * <pre>
    * {@code
    *    // Let a be a (2,2,3) tensor.
    *    a = {{  1, 2, 3,
    *            4, 5, 6 },
    *         {  7, 8, 9,
    *           -1,-2,-3 }}
    *
    *   //Performing
    *   a.add(Shape.ROW, 0, 2);
    *
    *   //Would result in a being
    *   {{ -1,  0,  3,
    *       4,  5,  6 },
    *    {  5,  6,  7,
    *      -1, -2, -3 }}
    * }
    * </pre>
    *
    * @param axis     the axis to subtract the given NDArray along
    * @param position the position of the axis to perform the subtract on
    * @param rhs      the number to subtract
    * @return this NDArray with the results of the subtraction
    */
   public NDArray<T> subi(int axis, int position, @NonNull Number rhs) {
      return mapiAxisDouble(axis, position, rhs.doubleValue(), Operator::subtract);
   }

   /**
    * <p>Calculates the sum of values along the given axis in the NDArray across slices. (Only supported by numeric
    * NDArray).</p>
    *
    * @return the sumb value
    */
   public NDArray<T> sum(int axis, int... other) {
      checkThisIsNumeric(this);
      return NDArrayOps.reduceDoubleAxis(this, Operator::add, axis, other);
   }

   /**
    * <p>Calculates the sum of all values in the NDArray across slices. (Only supported by numeric NDArrays)</>
    *
    * @return the sum
    */
   public double sum() {
      checkThisIsNumeric(this);
      return shape()
            .range()
            .stream()
            .mapToDouble(this::getDouble).reduce(0, Operator::add);
   }

   /**
    * <p>Calculates the sum of squares of the values along the given axis in the NDArray across slices. (Only supported
    * by numeric NDArray).</p>
    *
    * @return the sumb value
    */
   public NDArray<T> sumOfSquares(int axis, int... other) {
      checkThisIsNumeric(this);
      return NDArrayOps.reduceDoubleAxis(this, (a, b) -> a + (b * b), axis, other);
   }

   /**
    * <p>Calculates the sum of squares for all values in the NDArray across slices. (Only supported by numeric
    * NDArrays)</p>
    *
    * @return the sum of squares
    */
   public double sumOfSquares() {
      checkThisIsNumeric(this);
      return shape()
            .range()
            .stream()
            .mapToDouble(this::getDouble).reduce(0, (a, b) -> a + (b * b));
   }

   /**
    * Converts the NDArray into an array of DoubleMatrix. (one per slice)
    *
    * @return the array of DoubleMatrix
    */
   public DoubleMatrix[] toDoubleMatrix() {
      if (shape.isEmpty()) {
         return new DoubleMatrix[0];
      }
      if (shape.isScalar()) {
         return new DoubleMatrix[]{DoubleMatrix.scalar(scalarDouble())};
      }
      DoubleMatrix[] m = new DoubleMatrix[shape.sliceLength()];
      for (int i = 0; i < shape.sliceLength(); i++) {
         DoubleMatrix v = new DoubleMatrix(Math.max(1, shape.rows()), shape.columns());
         NDArray<T> n = slice(i);
         for (int j = 0; j < n.length(); j++) {
            v.put(j, n.getDouble(j));
         }
         m[i] = v;
      }
      return m;
   }


   /**
    * Converts the NDArray into an array of DoubleMatrix. (one per slice)
    *
    * @return the array of DoubleMatrix
    */
   public FloatMatrix[] toFloatMatrix() {
      if (shape.isEmpty()) {
         return new FloatMatrix[0];
      }
      if (shape.isScalar()) {
         return new FloatMatrix[]{FloatMatrix.scalar((float) scalarDouble())};
      }
      FloatMatrix[] m = new FloatMatrix[shape.sliceLength()];
      for (int i = 0; i < shape.sliceLength(); i++) {
         FloatMatrix v = new FloatMatrix(Math.max(1, shape.rows()), shape.columns());
         NDArray<T> n = slice(i);
         for (int j = 0; j < n.length(); j++) {
            v.put(j, (float) n.getDouble(j));
         }
         m[i] = v;
      }
      return m;
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
      return Tensor.create(arrayForTensor());
   }


   public NDArray<T> transpose(@NonNull int... newAxes) {
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
      NDArray<T> out = factory().zeros(shape().get(n[0]), shape().get(n[1]), shape().get(n[2]), shape().get(n[3]));
      for (Index index : shape().range()) {
         out.set(index.get(n[0]), index.get(n[1]), index.get(n[2]), index.get(n[3]), get(index));
      }
      return out;
   }

   /**
    * <p>Unitizes the NDArray by dividing the values by L2 Norm. (Only supported by numeric NDArrays)</p>
    *
    * @return Unitized version of this NDArray
    */
   public NDArray<Float> unitize() {
      checkThisIsNumeric(this);
      NDArray<Float> out = nd.DFLOAT32.zeros(shape);
      double norm2 = norm2();
      shape.range().forEach(ii -> out.set(ii, getDouble(ii) / norm2));
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
   public NDArray<T> zero() {
      return fill(Cast.as(Primitives.defaultValue(getType())));
   }

   /**
    * <p>Creates an NDArray of zeros with the same shape as this NDArray</p>
    *
    * @return the zero-valued NDArray
    */
   public NDArray<T> zeroLike() {
      return factory().zeros(shape().copy());
   }

   /**
    * Interface for processing individual entries of an NDArray
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

