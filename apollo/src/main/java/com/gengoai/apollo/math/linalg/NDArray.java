/*
 * (c) 2005 David B. Bracewell
 *
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
 *
 */

package com.gengoai.apollo.math.linalg;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.Copyable;
import com.gengoai.Validation;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.model.sequence.SequenceValidator;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.conversion.Cast;
import com.gengoai.math.Operator;
import com.gengoai.string.Strings;
import lombok.NonNull;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * An n-dimension array of double values used for vectors, matrices, and tensors.
 *
 * @author David B. Bracewell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
      @JsonSubTypes.Type(value = Tensor.class, name = "tensor"),
      @JsonSubTypes.Type(value = DenseMatrix.class, name = "dm"),
      @JsonSubTypes.Type(value = SparseMatrix.class, name = "sm")
})
@JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.NONE,
      setterVisibility = JsonAutoDetect.Visibility.NONE,
      getterVisibility = JsonAutoDetect.Visibility.NONE,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE,
      creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class NDArray implements Serializable, Observation {
   private static final long serialVersionUID = 1L;
   protected static final NumberFormat decimalFormatter = new DecimalFormat(" 0.000000;-0");
   @JsonProperty("shape")
   protected final Shape shape;
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
    * @param shape The shape of the new NDArray
    */
   protected NDArray(@NonNull Shape shape) {
      this.shape = shape.copy();
   }

   /**
    * Flips the matrix on its diagonal switching the rows and columns. (This is done per slice)
    *
    * @return the transposed array
    */
   public abstract NDArray T();

   /**
    * Adds a scalar value to each element in the NDArray
    *
    * @param value the value to add
    * @return the new NDArray with the scalar value added
    */
   public NDArray add(double value) {
      if(value == 0) {
         return copy();
      }
      return map(value, Operator::add);
   }

   /**
    * Adds the values in the other NDArray to this one.
    *
    * @param rhs the other NDArray whose values will be added
    * @return the new NDArray with the result of this + other
    */
   public NDArray add(@NonNull NDArray rhs) {
      return map(rhs, Operator::add);
   }

   /**
    * Adds the values in the other NDArray to each column in this one.
    *
    * @param rhs the other NDArray whose values will be added
    * @return the new NDArray
    */
   public NDArray addColumnVector(@NonNull NDArray rhs) {
      return mapColumn(rhs, Operator::add);
   }

   /**
    * Adds the values in the other NDArray to each row in this one.
    *
    * @param rhs the other NDArray whose values will be added
    * @return the new NDArray
    */
   public NDArray addRowVector(@NonNull NDArray rhs) {
      return mapRow(rhs, Operator::add);
   }

   /**
    * Adds a scalar value to each element in the NDArray in-place
    *
    * @param value the value to add
    * @return this NDArray with the scalar value added
    */
   public NDArray addi(double value) {
      if(value != 0) {
         return mapi(value, Operator::add);
      }
      return this;
   }

   /**
    * Adds the values in the other NDArray to this one in-place.
    *
    * @param rhs the other NDArray whose values will be added
    * @return this NDArray with the result of this + other
    */
   public NDArray addi(@NonNull NDArray rhs) {
      return mapi(rhs, Operator::add);
   }

   /**
    * Performs a column vector addition adding the values in the other NDArray to each column in this NDArray.
    *
    * @param rhs the other NDArray whose values will be added
    * @return this NDArray with the result of this + other
    */
   public NDArray addiColumnVector(@NonNull NDArray rhs) {
      return mapiColumn(rhs, Operator::add);
   }

   /**
    * Performs a row vector addition adding the values in the other NDArray to each row in this NDArray.
    *
    * @param rhs the other NDArray whose values will be added
    * @return this NDArray with the result of this + other
    */
   public NDArray addiRowVector(@NonNull NDArray rhs) {
      return mapiRow(rhs, Operator::add);
   }

   /**
    * Calculates the index in the NDArray with maximum value.
    *
    * @return the index with maximum value
    */
   public abstract long argmax();

   /**
    * Calculates the index in the NDArray with minimum value.
    *
    * @return the index with minimum value
    */
   public abstract long argmin();

   private double asDouble(Object object) {
      if(object == null) {
         return Double.NaN;
      } else if(object instanceof NDArray) {
         NDArray array = Cast.as(object);
         if(array.shape.isScalar()) {
            return array.scalar();
         }
         return array.argmax();
      }
      return Cast.<Number>as(object).doubleValue();
   }

   @Override
   public NDArray asNDArray() {
      return this;
   }

   private NDArray asNDArray(Object o, int dimension) {
      if(o == null) {
         return com.gengoai.apollo.math.linalg.NDArrayFactory.ND.empty();
      } else if(o instanceof Number) {
         Number numLabel = Cast.as(o);
         if(dimension == 1) {
            return com.gengoai.apollo.math.linalg.NDArrayFactory.ND.scalar(numLabel.floatValue());
         }
         return com.gengoai.apollo.math.linalg.NDArrayFactory.ND.array(dimension).set(numLabel.intValue(), 1f);
      }
      NDArray nd = Cast.as(o, NDArray.class);
      Validation.notNull(nd, "Cannot create NDArray from object.");
      return nd;
   }

   /**
    * Number of channels in the NDArray
    *
    * @return the number of channels in the NDArray
    */
   public int channels() {
      return shape.channels();
   }

   /**
    * Calculates the index of maximum values per column in the NDArray.
    *
    * @return the NDArray of column indexes with maximum value.
    */
   public abstract NDArray columnArgmaxs();

   /**
    * Calculates the index of minimum values per column in the NDArray.
    *
    * @return the NDArray of column indexes with minimum value.
    */
   public abstract NDArray columnArgmins();

   /**
    * Calculates the maximum values per column in the NDArray.
    *
    * @return the NDArray of maximum values per column.
    */
   public abstract NDArray columnMaxs();

   /**
    * Calculates the mean values per column in the NDArray.
    *
    * @return the NDArray of mean values per column.
    */
   public NDArray columnMeans() {
      return columnSums().divi(shape().rows());
   }

   /**
    * Calculates the minimum values per column in the NDArray.
    *
    * @return the NDArray of minimum values per column.
    */
   public abstract NDArray columnMins();

   /**
    * Calculates sums per column in the NDArray.
    *
    * @return the NDArray of sums per column.
    */
   public abstract NDArray columnSums();

   /**
    * Number of columns in the NDArray
    *
    * @return the number of columns in the NDArray
    */
   public int columns() {
      return shape.columns();
   }

   /**
    * Compacts the memory usages of sparse NDArrays.
    *
    * @return this NDArray
    */
   public abstract NDArray compact();

   public boolean isEmpty(){
      return shape.sliceLength == 0 && shape.matrixLength == 0;
   }

   @Override
   public NDArray copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Generates a diagonal matrix per slice.
    *
    * @return The NDArray with diagonal slices.
    */
   public abstract NDArray diag();

   /**
    * Divides the values in the other NDArray to this one element by element.
    *
    * @param rhs the other NDArray whose values will be divided
    * @return the new NDArray with the result of this / other
    */
   public NDArray div(@NonNull NDArray rhs) {
      return map(rhs, Operator::divide);
   }

   /**
    * Divides a scalar value to each element in the NDArray
    *
    * @param value the value to divide
    * @return the new NDArray with the scalar value divided
    */
   public NDArray div(double value) {
      return map(value, Operator::divide);
   }

   /**
    * Divides a column vector element division dividing the values in the other NDArray to each column in this NDArray.
    *
    * @param rhs the other NDArray whose values will be divided
    * @return the new NDArray with the result of this / other
    */
   public NDArray divColumnVector(@NonNull NDArray rhs) {
      return mapColumn(rhs, Operator::divide);
   }

   /**
    * Divides a row vector element division dividing the values in the other NDArray to each row in this NDArray.
    *
    * @param rhs the other NDArray whose values will be divided
    * @return the new NDArray with the result of this / other
    */
   public NDArray divRowVector(@NonNull NDArray rhs) {
      return mapRow(rhs, Operator::divide);
   }

   /**
    * Divides a scalar value to each element in the NDArray in-place.
    *
    * @param rhs the value to divide
    * @return this NDArray with the scalar value divided
    */
   public NDArray divi(@NonNull NDArray rhs) {
      return mapi(rhs, Operator::divide);
   }

   /**
    * Divides a scalar value to each element in the NDArray in-place.
    *
    * @param value the value to divide
    * @return this NDArray with the scalar value divided
    */
   public NDArray divi(double value) {
      return mapi(value, Operator::divide);
   }

   /**
    * Divides a column vector element division dividing the values in the other NDArray to each column in this NDArray.
    *
    * @param rhs the other NDArray whose values will be divided
    * @return this NDArray with the result of this / other
    */
   public NDArray diviColumnVector(@NonNull NDArray rhs) {
      return mapiColumn(rhs, Operator::divide);
   }

   /**
    * Divides a row vector element division dividing the values in the other NDArray to each row in this NDArray.
    *
    * @param rhs the other NDArray whose values will be divided
    * @return this NDArray with the result of this / other
    */
   public NDArray diviRowVector(@NonNull NDArray rhs) {
      return mapiRow(rhs, Operator::divide);
   }

   /**
    * Calculates the dot product between this and the given other NDArray per slice.
    *
    * @param rhs the NDArray to calculate the dot product with
    * @return NDArray of dot products
    */
   public abstract double dot(@NonNull NDArray rhs);

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is equal to the given value.
    *
    * @param value the value test equality for
    * @return the NDArray
    */
   public NDArray eq(double value) {
      return test(v -> v == value);
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is equal to the value in the other
    * NDArray.
    *
    * @param rhs the NDArray whose values to test equality for
    * @return the NDArray
    */
   public NDArray eq(@NonNull NDArray rhs) {
      return test(rhs, (v, value) -> v == value);
   }

   /**
    * Updates this NDArray element's to equal <code>1.0</code> if its value is equal to the given value.
    *
    * @param value the value test equality for
    * @return this NDArray
    */
   public NDArray eqi(double value) {
      return testi(v -> v == value);
   }

   /**
    * Updates this NDArray element's to qual  <code>1.0</code> if its value is equal to the value in the other NDArray.
    *
    * @param rhs the NDArray whose values to test equality for
    * @return this NDArray
    */
   public NDArray eqi(NDArray rhs) {
      return testi(rhs, (v, value) -> v == value);
   }

   /**
    * Fills the NDArray with the given value
    *
    * @param value the value to set all cells in the NDArray
    * @return This NDArray
    */
   public abstract NDArray fill(double value);

   /**
    * Processes the sparse entries in this NDArray
    *
    * @param consumer the consumer
    */
   public abstract void forEachSparse(@NonNull EntryConsumer consumer);

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is greater than or equal to the given
    * value.
    *
    * @param value the value to test
    * @return the NDArray
    */
   public NDArray ge(double value) {
      return test(v -> v >= value);
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is greater than or equal to the value
    * in the other NDArray.
    *
    * @param rhs the NDArray whose values to test
    * @return the NDArray
    */
   public NDArray ge(@NonNull NDArray rhs) {
      return test(rhs, (v, value) -> v >= value);
   }

   /**
    * Updates this NDArray element's to equal <code>1.0</code> if its value is greater than or equal to the given
    * value.
    *
    * @param value the value to test
    * @return this NDArray
    */
   public NDArray gei(double value) {
      return testi(v -> v >= value);
   }

   /**
    * Updates this NDArray element's to equal  <code>1.0</code> if its value is greater than or equal to the value in
    * the other NDArray.
    *
    * @param rhs the NDArray whose values to test
    * @return this NDArray
    */
   public NDArray gei(@NonNull NDArray rhs) {
      return testi(rhs, (v, value) -> v >= value);
   }

   /**
    * Gets the value at the given index (row/column if vector, entry if other)
    *
    * @param i the index
    * @return the double value
    */
   public abstract double get(long i);

   /**
    * Gets the value of the NDArray at the given row and column. (Assumes channel and kernel are 0)
    *
    * @param row the row index
    * @param col the column index
    * @return the double value
    */
   public abstract double get(int row, int col);

   /**
    * Gets the value of the NDArray at the given channel, row, and column (Assumes kernel is 0)
    *
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @return the double value
    */
   public abstract double get(int channel, int row, int col);

   /**
    * Gets the value of the NDArray at the given kernel, channel, row, and column
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @return the double value
    */
   public abstract double get(int kernel, int channel, int row, int col);

   /**
    * Creates an NDArray made up of the column at the given index for each slice. (Note modifications to the new NDArray
    * do  not effect this one).
    *
    * @param column the column index
    * @return the column NDArray
    */
   public abstract NDArray getColumn(int column);

   /**
    * Creates an NDArray made up of only the columns given.
    *
    * @param columns the rows to extract
    * @return the new NDArray
    */
   public abstract NDArray getColumns(int[] columns);

   /**
    * Creates an NDArray made up of the columns starting at the given from index and ending at (not-including) the to
    * index.
    *
    * @param from the starting index
    * @param to   the ending index
    * @return the new NDArray
    */
   public abstract NDArray getColumns(int from, int to);

   /**
    * Gets the label associated with the NDArray
    *
    * @param <T> the type of the label
    * @return the label
    */
   public <T> T getLabel() {
      return Cast.as(label);
   }

   /**
    * Gets the label associated with the NDArray as a double value.
    *
    * @return the label as double
    */
   public double getLabelAsDouble() {
      return asDouble(label);
   }

   /**
    * Gets the label associated with the NDArray as an NDArray
    *
    * @return the label as NDArray
    */
   public NDArray getLabelAsNDArray() {
      return getLabelAsNDArray(1);
   }

   /**
    * Gets the label associated with this NDArray as an NDArray (vector) with desired dimension.
    *
    * @param dimension the dimension
    * @return the label as nd array
    */
   public NDArray getLabelAsNDArray(int dimension) {
      return asNDArray(label, dimension);
   }

   /**
    * Gets the predicted label associated with this NDArray.
    *
    * @param <T> the type parameter
    * @return the predicted label
    */
   public <T> T getPredicted() {
      return Cast.as(predicted);
   }

   /**
    * Gets the predicted label associated with the NDArray as a double value.
    *
    * @return the predicted label as double
    */
   public double getPredictedAsDouble() {
      return asDouble(predicted);
   }

   /**
    * Gets the predicted label associated with the NDArray as an NDArray
    *
    * @return the predicted label as NDArray
    */
   public NDArray getPredictedAsNDArray() {
      return asNDArray(predicted, 1);
   }

   /**
    * Gets the predicted label associated with this NDArray as an NDArray (vector) with desired dimension.
    *
    * @param dimension the dimension
    * @return the predicted label as NDArray
    */
   public NDArray getPredictedAsNDArray(int dimension) {
      return asNDArray(predicted, dimension);
   }

   /**
    * Creates an NDArray made up of the row at the given index for each slice. (Note modifications to the new NDArray do
    * not effect this one).
    *
    * @param row the row index
    * @return the row NDArray
    */
   public abstract NDArray getRow(int row);

   /**
    * Creates an NDArray made up of only the rows given.
    *
    * @param rows the rows to extract
    * @return the new NDArray
    */
   public abstract NDArray getRows(int[] rows);

   /**
    * Creates an NDArray made up of the rows starting at the given from index and ending at (not-including) the to
    * index.
    *
    * @param from the starting index
    * @param to   the ending index
    * @return the new NDArray
    */
   public abstract NDArray getRows(int from, int to);

   /**
    * Creates a new NDArray made up of sub-portions of the slices.
    *
    * @param fromRow the index of the row to start slicing from
    * @param toRow   the index of the row to end the slicing at
    * @param fromCol the index of the column to start slicing from
    * @param toCol   the index of the column to end slicing at
    * @return the NDArray
    */
   public abstract NDArray getSubMatrix(int fromRow, int toRow, int fromCol, int toCol);

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
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is greater than  to the given value.
    *
    * @param value the value to test
    * @return the NDArray
    */
   public NDArray gt(double value) {
      return test(v -> v > value);
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is greater than to the value in the
    * other NDArray.
    *
    * @param rhs the NDArray whose values to test
    * @return the NDArray
    */
   public NDArray gt(@NonNull NDArray rhs) {
      return test(rhs, (v, value) -> v > value);
   }

   /**
    * Updates this NDArray element's to equal <code>1.0</code> if its value is greater than to the given value.
    *
    * @param value the value to test
    * @return this NDArray
    */
   public NDArray gti(double value) {
      return testi(v -> v > value);
   }

   /**
    * Updates this NDArray element's to equal  <code>1.0</code> if its value is greater than to the value in the other
    * NDArray.
    *
    * @param rhs the NDArray whose values to test
    * @return this NDArray
    */
   public NDArray gti(@NonNull NDArray rhs) {
      return testi(rhs, (v, value) -> v > value);
   }

   public abstract NDArray incrementiColumn(int c, NDArray vector);

   /**
    * Checks if the NDArray is made up of dense slices
    *
    * @return True if the NDArray is made up of dense slices, False otherwise
    */
   public abstract boolean isDense();

   @Override
   public boolean isNDArray() {
      return true;
   }

   /**
    * Number of kernels in the NDArray
    *
    * @return the number of kernels in the NDArray
    */
   public int kernels() {
      return shape.kernels();
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is less than or equal to the given
    * value.
    *
    * @param value the value to test
    * @return the NDArray
    */
   public NDArray le(double value) {
      return test(v -> v <= value);
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is less than or equal to the value in
    * the other NDArray.
    *
    * @param rhs the NDArray whose values to test
    * @return the NDArray
    */
   public NDArray le(@NonNull NDArray rhs) {
      return test(rhs, (v, value) -> v <= value);
   }

   /**
    * Updates this NDArray element's to equal <code>1.0</code> if its value is less than or equal to the given value.
    *
    * @param value the value to test
    * @return this NDArray
    */
   public NDArray lei(double value) {
      return testi(v -> v <= value);
   }

   /**
    * Updates this NDArray element's to equal  <code>1.0</code> if its value is less than or equal to the value in the
    * other NDArray.
    *
    * @param rhs the NDArray whose values to test
    * @return this NDArray
    */
   public NDArray lei(@NonNull NDArray rhs) {
      return testi(rhs, (v, value) -> v <= value);
   }

   /**
    * The total number of elements. (<code>kernels * channels * rows * columns</code>)
    *
    * @return the length (total number) of the elements in the NDArray
    */
   public long length() {
      return shape.sliceLength * shape.matrixLength;
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is less than to the given value.
    *
    * @param value the value to test
    * @return the NDArray
    */
   public NDArray lt(double value) {
      return test(v -> v < value);
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is less than to the value in the other
    * NDArray.
    *
    * @param rhs the NDArray whose values to test
    * @return the NDArray
    */
   public NDArray lt(@NonNull NDArray rhs) {
      return test(rhs, (v, value) -> v < value);
   }

   /**
    * Updates this NDArray element's to equal <code>1.0</code> if its value is less than the given value.
    *
    * @param value the value to test
    * @return this NDArray
    */
   public NDArray lti(double value) {
      return testi(v -> v < value);
   }

   /**
    * Updates this NDArray element's to equal  <code>1.0</code> if its value is less than to the value in the other
    * NDArray.
    *
    * @param rhs the NDArray whose values to test
    * @return this NDArray
    */
   public NDArray lti(@NonNull NDArray rhs) {
      return testi(rhs, (v, value) -> v < value);
   }

   /**
    * Creates a new NDArray with values from this NDArray evaluated using the given unary operator.
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray map(@NonNull DoubleUnaryOperator operator);

   /**
    * Creates a new NDArray with values from this NDArray evaluated by the given binary operation with the given value.
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public abstract NDArray map(double value, @NonNull DoubleBinaryOperator operator);

   /**
    * Creates a new NDArray with values from this NDArray and the given NDArray evaluated using the given  binary
    * operation.
    *
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray map(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Creates a new NDArray with values from this NDArray and the given NDArray evaluated using the given  binary
    * operation per column.
    *
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapColumn(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Updates the values in the given column of  this NDArray by performing the given binary operation with the values
    * in the given NDArray.
    *
    * @param column   the column whose values we want to manipulate
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapColumn(int column, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Creates a new NDArray with values from this NDArray and the given NDArray evaluated using the given  binary
    * operation per row.
    *
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapRow(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Updates the values in the given row of  this NDArray by performing the given binary operation with the values in
    * the given NDArray.
    *
    * @param row      the row whose values we want to manipulate
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapRow(int row, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   @Override
   public void mapVariables(@NonNull Function<Variable, Variable> mapper) {
      throw new UnsupportedOperationException("NDArray does not support mapping.");
   }

   /**
    * Updates the values in this NDArray evaluated using the given unary operator.
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapi(@NonNull DoubleUnaryOperator operator);

   /**
    * Updates the values in this NDArray by performing he given binary operation with the given value.
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public abstract NDArray mapi(double value, @NonNull DoubleBinaryOperator operator);

   /**
    * Updates the values int this NDArray by performing the given binary operation with the values in the given
    * NDArray.
    *
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapi(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Updates the values int this NDArray by performing the given binary operation with the values in the given NDArray
    * per column.
    *
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapiColumn(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Updates the values in the given column of  this NDArray by performing the given binary operation with the values
    * in the given NDArray.
    *
    * @param column   the column whose values we want to manipulate
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapiColumn(int column, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Updates the values in the given row of  this NDArray by performing the given binary operation with the values in
    * the given NDArray.
    *
    * @param row      the row whose values we want to manipulate
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapiRow(int row, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Updates the values int this NDArray by performing the given binary operation with the values in the given NDArray
    * per row.
    *
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public abstract NDArray mapiRow(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator);

   /**
    * Calculates the maximum value in the NDArray.
    *
    * @return the maximum value
    */
   public abstract double max();

   /**
    * Calculates the mean value in the NDArray
    *
    * @return the mean value
    */
   public double mean() {
      return sum() / (shape().matrixLength * shape().sliceLength);
   }

   /**
    * Calculates the minimum value in the NDArray
    *
    * @return the minimum value
    */
   public abstract double min();

   /**
    * Creates a new NDArray by multiplying the (matrix) slices of this NDArray with those in the given NDArray.
    *
    * @param rhs the NDArray to multiply
    * @return the resulting NDArray
    */
   public abstract NDArray mmul(@NonNull NDArray rhs);

   /**
    * Multiplies the values in the other NDArray to this one element by element.
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return the new NDArray with the result of this * other
    */
   public NDArray mul(@NonNull NDArray rhs) {
      return map(rhs, Operator::multiply);
   }

   /**
    * Multiplies a scalar value to each element in the NDArray
    *
    * @param value the value to multiplied
    * @return the new NDArray with the scalar value multiplied
    */
   public NDArray mul(double value) {
      return map(value, Operator::multiply);
   }

   /**
    * Performs a column vector element multiplication multiplying the values in the other NDArray to each  in this
    * NDArray.
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return the new NDArray with the result of this * other
    */
   public NDArray mulColumnVector(@NonNull NDArray rhs) {
      return mapColumn(rhs, Operator::multiply);
   }

   /**
    * Performs a row vector element multiplication multiplying the values in the other NDArray to each row  in this
    * NDArray.
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return the new NDArray with the result of this * other
    */
   public NDArray mulRowVector(@NonNull NDArray rhs) {
      return mapRow(rhs, Operator::multiply);
   }

   /**
    * Multiplies the values in the other NDArray to this one element by element in-place.
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return this NDArray with the result of this * other
    */
   public NDArray muli(@NonNull NDArray rhs) {
      return mapi(rhs, Operator::multiply);
   }

   /**
    * Multiplies a scalar value to each element in the NDArray in-place.
    *
    * @param value the value to multiplied
    * @return this NDArray with the scalar value multiplied
    */
   public NDArray muli(double value) {
      return mapi(value, Operator::multiply);
   }

   /**
    * Performs a column vector element multiplication multiplying the values in the other NDArray to each  in this
    * NDArray in-place.
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return the new NDArray with the result of this * other
    */
   public NDArray muliColumnVector(@NonNull NDArray rhs) {
      return mapiColumn(rhs, Operator::multiply);
   }

   /**
    * Performs a row vector element multiplication multiplying the values in the other NDArray to each row  in this
    * NDArray in-place.
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return the new NDArray with the result of this * other
    */
   public NDArray muliRowVector(@NonNull NDArray rhs) {
      return mapiRow(rhs, Operator::multiply);
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is not equal to the given value.
    *
    * @param value the value test equality for
    * @return the NDArray
    */
   public NDArray neq(double value) {
      return testi(v -> v != value);
   }

   /**
    * Creates a new NDArray with elements equal to <code>1.0</code> if its value is not equal to the value in the other
    * NDArray.
    *
    * @param rhs the NDArray whose values to test equality for
    * @return the NDArray
    */
   public NDArray neq(@NonNull NDArray rhs) {
      return testi(rhs, (v, value) -> v != value);
   }

   /**
    * Updates this NDArray element's to equal <code>1.0</code> if its value is not equal to the given value.
    *
    * @param value the value test equality for
    * @return this NDArray
    */
   public NDArray neqi(double value) {
      return testi(v -> v != value);
   }

   /**
    * Updates this NDArray element's to equal  <code>1.0</code> if its value is not equal to the value in the other
    * NDArray.
    *
    * @param rhs the NDArray whose values to test equality for
    * @return this NDArray
    */
   public NDArray neqi(@NonNull NDArray rhs) {
      return testi(rhs, (v, value) -> v != value);
   }

   /**
    * Calculates the L1 norm of the NDArray
    *
    * @return the L1 norm of the NDArray
    */
   public abstract double norm1();

   /**
    * Calculates the L2 norm of the NDArray
    *
    * @return the L2 norm of the NDArray
    */
   public abstract double norm2();

   public abstract NDArray padColumnPost(int maxLength);

   public abstract NDArray padPost(int maxRowLength, int maxColumnLength);

   public abstract NDArray padRowPost(int maxLength);

   /**
    * Calculates the pivot elements for this square matrix. Will calculate per slice.
    *
    * @return A NDArray of 1's and 0's representing pivot elements.
    */
   public abstract NDArray pivot();

   private void printSlice(NDArray slice, int maxR, int maxC, StringBuilder builder) {
      builder.append("[");
      builder.append(rowToString(slice, 0, maxC));
      int breakPoint = maxR / 2;
      for(int i = 1; i < slice.rows(); i++) {
         builder.append(",");
         if(i == breakPoint) {
            int nj = Math.max(slice.rows() - breakPoint, i + 1);
            if(nj > i + 1) {
               builder.append(System.lineSeparator()).append("     ...").append(System.lineSeparator());
            }
            i = nj;
         }
         builder.append(System.lineSeparator()).append("  ").append(rowToString(slice, i, maxC));
      }
      builder.append("]");
   }

   /**
    * Divides the values in the this NDArray from the other NDArray.
    *
    * @param lhs the other NDArray whose values will be divided from
    * @return the new NDArray with the result of other / this
    */
   public NDArray rdiv(@NonNull NDArray lhs) {
      return map(lhs, (v1, v2) -> v2 / v1);
   }

   /**
    * Divides each element's value from the given scalar (e.g. scalar - element)
    *
    * @param value the value to divide
    * @return the new NDArray with the scalar value divided
    */
   public NDArray rdiv(double value) {
      return map(value, (v1, v2) -> v2 / v1);
   }

   /**
    * Performs a column vector division dividing the values in this NDArray from the other NDArray to each column in
    * this NDArray.
    *
    * @param lhs the other NDArray whose values will be divided
    * @return the new NDArray with the result of this / other
    */
   public NDArray rdivColumnVector(@NonNull NDArray lhs) {
      return mapColumn(lhs, (v1, v2) -> v2 / v1);
   }

   /**
    * Performs a row vector division dividing the values in this NDArray from the other NDArray to each row in this
    * NDArray.
    *
    * @param lhs the other NDArray whose values will be divided
    * @return the new NDArray with the result of this / other
    */
   public NDArray rdivRowVector(@NonNull NDArray lhs) {
      return mapRow(lhs, (v1, v2) -> v2 / v1);
   }

   /**
    * Divides the values in the this NDArray from the other NDArray in-place.
    *
    * @param lhs the other NDArray whose values will be divided from
    * @return the new NDArray with the result of other / this
    */
   public NDArray rdivi(@NonNull NDArray lhs) {
      return mapi(lhs, (v1, v2) -> v2 / v1);
   }

   /**
    * Divides each element's value from the given scalar (e.g. scalar - element) in-place
    *
    * @param value the value to divide
    * @return the new NDArray with the scalar value divided
    */
   public NDArray rdivi(double value) {
      return mapi(value, (v1, v2) -> v2 / v1);
   }

   /**
    * Performs a column vector division dividing the values in this NDArray from the other NDArray to each column in
    * this NDArray in-place.
    *
    * @param lhs the other NDArray whose values will be divided
    * @return the new NDArray with the result of this / other
    */
   public NDArray rdiviColumnVector(@NonNull NDArray lhs) {
      return mapiColumn(lhs, (v1, v2) -> v2 / v1);
   }

   /**
    * Performs a row vector division dividing the values in this NDArray from the other NDArray to each row in this
    * NDArray in-place.
    *
    * @param lhs the other NDArray whose values will be divided
    * @return the new NDArray with the result of this / other
    */
   public NDArray rdiviRowVector(@NonNull NDArray lhs) {
      return mapiRow(lhs, (v1, v2) -> v2 / v1);
   }

   @Override
   public void removeVariables(@NonNull Predicate<Variable> filter) {
      throw new UnsupportedOperationException("NDArray does not support filtering.");
   }

   /**
    * Reshapes the NDArray
    *
    * @param dims the new dimensions of the NDArray
    * @return this NDArray with new shape
    */
   public abstract NDArray reshape(int... dims);

   /**
    * Calculates the index of maximum values per row in the NDArray.
    *
    * @return the NDArray of row indexes with maximum value.
    */
   public abstract NDArray rowArgmaxs();

   /**
    * Calculates the index of minimum values per row in the NDArray.
    *
    * @return the NDArray of row indexes with minimum value.
    */
   public abstract NDArray rowArgmins();

   /**
    * Calculates the maximum values per row in the NDArray.
    *
    * @return the NDArray of maximum values per row.
    */
   public abstract NDArray rowMaxs();

   /**
    * Calculates the mean values per row in the NDArray.
    *
    * @return the NDArray of mean values per row.
    */
   public NDArray rowMeans() {
      return rowSums().divi(shape().columns());
   }

   /**
    * Calculates the minimum values per row in the NDArray.
    *
    * @return the NDArray of minimum values per row.
    */
   public abstract NDArray rowMins();

   /**
    * Calculates the sum per row in the NDArray.
    *
    * @return the NDArray of sum per row.
    */
   public abstract NDArray rowSums();

   private String rowToString(NDArray slice, int i, int maxC) {
      StringBuilder builder = new StringBuilder("[");
      builder.append(decimalFormatter.format(slice.get(i, 0)));
      int breakPoint = maxC / 2;
      for(int j = 1; j < slice.columns(); j++) {
         if(j == breakPoint) {
            int nj = Math.max(slice.columns() - breakPoint, j + 1);
            if(nj > j + 1 && nj < slice.columns()) {
               builder.append(", ...");
            }
            if(nj < slice.columns()) {
               j = nj;
            } else {
               continue;
            }
         }
         builder.append(", ").append(decimalFormatter.format(slice.get(i, j)));
      }
      return builder.append("]").toString();
   }

   /**
    * Number of rows in the NDArray
    *
    * @return the number of rows in the NDArray
    */
   public int rows() {
      return shape.rows();
   }

   /**
    * Subtracts the values in the this NDArray from the other NDArray.
    *
    * @param lhs the other NDArray whose values will be subtracted from
    * @return the new NDArray with the result of other - this
    */
   public NDArray rsub(@NonNull NDArray lhs) {
      return map(lhs, (v1, v2) -> v2 - v1);
   }

   /**
    * Subtracts each element's value from the given scalar (e.g. scalar - element)
    *
    * @param value the value to subtract
    * @return the new NDArray with the scalar value subtracted
    */
   public NDArray rsub(double value) {
      return map(value, (v1, v2) -> v2 - v1);
   }

   /**
    * Performs a column vector subtraction subtracting the values in this NDArray from the other NDArray to each column
    * in this NDArray.
    *
    * @param lhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray rsubColumnVector(@NonNull NDArray lhs) {
      return mapColumn(lhs, (v1, v2) -> v2 - v1);
   }

   /**
    * Performs a row vector subtraction subtracting the values in this NDArray from the other NDArray to each row in
    * this NDArray.
    *
    * @param lhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray rsubRowVector(@NonNull NDArray lhs) {
      return mapRow(lhs, (v1, v2) -> v2 - v1);
   }

   /**
    * Subtracts the values in the this NDArray from the other NDArray in-place.
    *
    * @param lhs the other NDArray whose values will be subtracted from
    * @return the new NDArray with the result of other - this
    */
   public NDArray rsubi(@NonNull NDArray lhs) {
      return mapi(lhs, (v1, v2) -> v2 - v1);
   }

   /**
    * Subtracts each element's value from the given scalar (e.g. scalar - element) in-place
    *
    * @param value the value to subtract
    * @return the new NDArray with the scalar value subtracted
    */
   public NDArray rsubi(double value) {
      return mapi(value, (v1, v2) -> v2 - v1);
   }

   /**
    * Performs a column vector subtraction subtracting the values in this NDArray from the other NDArray to each column
    * in this NDArray in-place.
    *
    * @param lhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray rsubiColumnVector(@NonNull NDArray lhs) {
      return mapiColumn(lhs, (v1, v2) -> v2 - v1);
   }

   /**
    * Performs a row vector subtraction subtracting the values in this NDArray from the other NDArray to each row in
    * this NDArray in-place.
    *
    * @param lhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray rsubiRowVector(@NonNull NDArray lhs) {
      return mapiRow(lhs, (v1, v2) -> v2 - v1);
   }

   /**
    * Returns the scalar value of this NDArray (value at <code>(0,0,0,0)</code>)
    *
    * @return the scalar value
    */
   public double scalar() {
      return get(0);
   }

   /**
    * Selects all values matching the given predicate.
    *
    * @param predicate the predicate to test
    * @return new NDArray with values passing the given predicate and zeros elsewhere
    */
   public NDArray select(@NonNull DoublePredicate predicate) {
      return map(v -> predicate.test(v)
                      ? v
                      : 0.0);
   }

   /**
    * Selects all values for which the corresponding element in the given NDArray has a value of <code>1.0</code>.
    *
    * @param rhs the NDArray used to determine which values are selected
    * @return the selected NDArray
    */
   public NDArray select(@NonNull NDArray rhs) {
      return map(rhs,
                 (v1, v2) -> v2 == 1.0
                             ? 1.0
                             : 0.0);
   }

   /**
    * Selects all values matching the given predicate in-place.
    *
    * @param predicate the predicate to test
    * @return this NDArray with values passing the given predicate and zeros elsewhere
    */
   public NDArray selecti(@NonNull DoublePredicate predicate) {
      return mapi(v -> predicate.test(v)
                       ? v
                       : 0.0);
   }

   /**
    * Selects all values for which the corresponding element in the given NDArray has a value of <code>1.0</code>
    * in-place.
    *
    * @param rhs the NDArray used to determine which values are selected
    * @return the selected NDArray
    */
   public NDArray selecti(@NonNull NDArray rhs) {
      return mapi(rhs,
                  (v1, v2) -> v2 == 1.0
                              ? 1.0
                              : 0.0);
   }

   /**
    * Sets the value of the element at the given index. (row/column if vector, entry if other)
    *
    * @param i     the index
    * @param value the value
    * @return this NDArray
    */
   public abstract NDArray set(long i, double value);

   /**
    * Sets the value of the element at the given row and column (assumes kernel and channel are 0).
    *
    * @param row   the row index
    * @param col   the column index
    * @param value the value
    * @return this NDArray
    */
   public abstract NDArray set(int row, int col, double value);

   /**
    * Sets the value of the element at the given channel, row, and column (assumes kernel is 0).
    *
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @param value   the value
    * @return this NDArray
    */
   public abstract NDArray set(int channel, int row, int col, double value);

   /**
    * Sets the value of the element at the given kernel, channel, row, and column
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @param row     the row index
    * @param col     the column index
    * @param value   the value
    * @return this NDArray
    */
   public abstract NDArray set(int kernel, int channel, int row, int col, double value);

   /**
    * Sets the values of the <code>ith</code> column to those in the given NDArray.
    *
    * @param i     the column index
    * @param array the array of new column values
    * @return this NDArray
    */
   public abstract NDArray setColumn(int i, @NonNull NDArray array);

   /**
    * Sets the label associated with the NDArray
    *
    * @param label the label
    * @return This NDArray
    */
   public NDArray setLabel(Object label) {
      this.label = label;
      return this;
   }

   /**
    * Sets the matrix associated with the given kernel and channel.
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @param array   the matrix
    * @return this NDArray
    */
   public NDArray setMatrix(int kernel, int channel, @NonNull NDArray array) {
      return setSlice(shape.sliceIndex(kernel, channel), array);
   }

   /**
    * Sets the predicted label for this NDArray.
    *
    * @param predicted the predicted label
    * @return this NDArray
    */
   public NDArray setPredicted(Object predicted) {
      this.predicted = predicted;
      return this;
   }

   /**
    * Sets the values of the <code>ith</code> row to those in the given NDArray.
    *
    * @param i     the row index
    * @param array the array of new row values
    * @return this NDArray
    */
   public abstract NDArray setRow(int i, @NonNull NDArray array);

   /**
    * Sets the slice at the given index.
    *
    * @param slice the slice index
    * @param array the NDArray of values for the new slice
    * @return this NDArray
    */
   public abstract NDArray setSlice(int slice, @NonNull NDArray array);

   /**
    * Sets the weight associated with the NDArray.
    *
    * @param weight the weight
    * @return this NDArray
    */
   public NDArray setWeight(double weight) {
      this.weight = (float) weight;
      return this;
   }

   /**
    * Gets the shape of the NDArray.
    *
    * @return the shape
    */
   public final Shape shape() {
      return shape;
   }

   /**
    * Then number of sparse entries (dense NDArray will have <code>size()=length()</code>)
    *
    * @return the number of sparse entries.
    */
   public long size() {
      return length();
   }

   /**
    * Returns a  view of a single slice of this NDArray. Note that changes to the slice will effect this NDArray.
    *
    * @param slice the slice index
    * @return the NDArray for the slice
    */
   public abstract NDArray slice(int slice);

   /**
    * Calculates the index of the per-slice maximum values.
    *
    * @return the per-slice argmax
    */
   public abstract NDArray sliceArgmaxs();

   /**
    * Calculates the index of the per-slice minimum values.
    *
    * @return the per-slice argmin
    */
   public abstract NDArray sliceArgmins();

   /**
    * Calculates the dot product between each slice of this and the given NDArray
    *
    * @param rhs the NDArray to calculate the dot product with
    * @return the per-slice dot product
    */
   public abstract NDArray sliceDot(NDArray rhs);

   /**
    * Calculates the  per-slice maximum values.
    *
    * @return the per-slice maximum values
    */
   public abstract NDArray sliceMaxs();

   /**
    * Calculates the  per-slice mean values.
    *
    * @return the per-slice mean values
    */
   public abstract NDArray sliceMeans();

   /**
    * Calculates the  per-slice minimum values.
    *
    * @return the per-slice minimum values
    */
   public abstract NDArray sliceMins();

   /**
    * Calculates the  per-slice L1 norm values.
    *
    * @return the per-slice L1 norm  values
    */
   public abstract NDArray sliceNorm1();

   /**
    * Calculates the  per-slice L2 norm values.
    *
    * @return the per-slice L2 norm  values
    */
   public abstract NDArray sliceNorm2();

   /**
    * Calculates the  per-slice sum of square values.
    *
    * @return the per-slice sum of square  values
    */
   public abstract NDArray sliceSumOfSquares();

   /**
    * Calculates the  per-slice sums.
    *
    * @return the per-slice sums
    */
   public abstract NDArray sliceSums();

   /**
    * Gets the indices of the sparse entries
    *
    * @return the index array
    */
   public abstract int[] sparseIndices();

   /**
    * Calculates the sparsity (Percentage of elements with a zero value) of the NDArray
    *
    * @return the sparsity (will equal to 1 if dense)
    */
   public double sparsity() {
      return 1.0 - ((double) size()) / ((double) length());
   }

   /**
    * Subtracts the values in the other NDArray to this one.
    *
    * @param rhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray sub(@NonNull NDArray rhs) {
      return map(rhs, Operator::subtract);
   }

   /**
    * Subtracts a scalar value to each element in the NDArray
    *
    * @param value the value to subtract
    * @return the new NDArray with the scalar value subtracted
    */
   public NDArray sub(double value) {
      return map(value, Operator::subtract);
   }

   /**
    * Performs a column vector subtraction subtracting the values in the other NDArray to each column in this NDArray.
    *
    * @param rhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray subColumnVector(@NonNull NDArray rhs) {
      return mapColumn(rhs, Operator::subtract);
   }

   /**
    * Performs a row vector subtraction subtracting the values in the other NDArray to each row in this NDArray.
    *
    * @param rhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray subRowVector(@NonNull NDArray rhs) {
      return mapRow(rhs, Operator::subtract);
   }

   /**
    * Subtracts the values in the other NDArray to this one in-place.
    *
    * @param rhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray subi(@NonNull NDArray rhs) {
      return mapi(rhs, Operator::subtract);
   }

   /**
    * Subtracts a scalar value to each element in the NDArray in-place.
    *
    * @param value the value to subtract
    * @return the new NDArray with the scalar value subtracted
    */
   public NDArray subi(double value) {
      return mapi(value, Operator::subtract);
   }

   /**
    * Performs a column vector subtraction subtracting the values in the other NDArray to each column in this NDArray
    * in-place.
    *
    * @param rhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray subiColumnVector(@NonNull NDArray rhs) {
      return mapiColumn(rhs, Operator::subtract);
   }

   /**
    * Performs a row vector subtraction subtracting the values in the other NDArray to each row in this NDArray
    * in-place.
    *
    * @param rhs the other NDArray whose values will be subtracted
    * @return the new NDArray with the result of this - other
    */
   public NDArray subiRowVector(@NonNull NDArray rhs) {
      return mapiRow(rhs, Operator::subtract);
   }

   /**
    * Calculates the sum of all values in the NDArray
    *
    * @return the sum
    */
   public abstract double sum();

   /**
    * Calculates the sum of squares for all values in the NDArray
    *
    * @return the sum of squares
    */
   public abstract double sumOfSquares();

   /**
    * Tests the given predicate on the values in the NDArray returning 1 when TRUE and 0 when FALSE
    *
    * @param predicate the predicate to test
    * @return new NDArray with test results
    */
   public NDArray test(@NonNull DoublePredicate predicate) {
      return map(v -> {
         if(predicate.test(v)) {
            return 1.0;
         }
         return 0d;
      });
   }

   /**
    * Compares entries in this NDArray with the given NDArray using the given comparison, setting entries to
    * <code>1.0</code> if the comparison returns true and <code>0.0</code> otherwise.
    *
    * @param rhs       the other NDArray
    * @param predicate the predicate
    * @return the NDArray with test results
    */
   public NDArray test(@NonNull NDArray rhs, @NonNull DoubleBinaryPredicate predicate) {
      return map(rhs, (v1, v2) -> {
         if(predicate.test(v1, v2)) {
            return 1.0;
         }
         return 0d;
      });
   }

   /**
    * Tests the given predicate on the values in the NDArray returning 1 when TRUE and 0 when FALSE. (in-place)
    *
    * @param predicate the predicate to test
    * @return new NDArray with test results
    */
   public NDArray testi(@NonNull DoublePredicate predicate) {
      return mapi(v -> {
         if(predicate.test(v)) {
            return 1.0;
         }
         return 0d;
      });
   }

   /**
    * Compares entries in this NDArray with the given NDArray using the given comparison, setting entries to
    * <code>1.0</code> if the comparison returns true and <code>0.0</code> otherwise. (in-place)
    *
    * @param rhs       the other NDArray
    * @param predicate the predicate
    * @return the NDArray with test results
    */
   public NDArray testi(@NonNull NDArray rhs, @NonNull DoubleBinaryPredicate predicate) {
      return mapi(rhs, (v1, v2) -> {
         if(predicate.test(v1, v2)) {
            return 1.0;
         }
         return 0d;
      });
   }

   /**
    * Converts the NDArray to double array
    *
    * @return the double array
    */
   public abstract double[] toDoubleArray();

   /**
    * Converts the NDArray into an array of DoubleMatrix. (one per slice)
    *
    * @return the array of DoubleMatrix
    */
   public abstract DoubleMatrix[] toDoubleMatrix();

   /**
    * Converts the NDArray to double array
    *
    * @return the double array
    */
   public abstract float[] toFloatArray();

   public abstract float[][] toFloatArray2();

   public abstract float[][][] toFloatArray3();

   public abstract FloatMatrix[] toFloatMatrix();

   @Override
   public String toString() {
      return toString(4, 10, 10);
   }

   /**
    * Generates a string form of the NDArray with a maximum number of slices, rows, and columns
    *
    * @param maxSlices  the max slices
    * @param maxRows    the max rows
    * @param maxColumns the max columns
    * @return the string
    */
   public String toString(int maxSlices, int maxRows, int maxColumns) {
      StringBuilder builder = new StringBuilder("[");

      if(shape.isVector()) {
         for(long i = 0; i < length(); i++) {
            if(i > 0) {
               builder.append(", ");
            }
            builder.append(get((int) i));
         }
         return builder.append("]").toString();
      }
      String outDot = Strings.repeat(Strings.padStart(".", 8, ' '), Math.min(columns(), maxColumns + 2));
      printSlice(slice(0), maxRows, maxColumns, builder);
      int breakPoint = maxSlices / 2;
      for(int i = 1; i < shape.sliceLength; i++) {
         builder.append(",");
         if(i == breakPoint) {
            int nj = Math.max(shape.sliceLength - breakPoint, i + 1);
            if(nj > i + 1) {
               builder.append(System.lineSeparator())
                      .append(System.lineSeparator()).append(outDot)
                      .append(System.lineSeparator()).append(outDot)
                      .append(System.lineSeparator()).append(outDot)
                      .append(System.lineSeparator())
                      .append(System.lineSeparator());

            }
            i = nj;
         }
         builder.append(System.lineSeparator()).append(" ");
         printSlice(slice(i), maxRows, maxColumns, builder);
      }
      return builder.toString();
   }

   /**
    * Unitizes the NDArray by dividing the values by L2 Norm (per slice)
    *
    * @return Unitized version of this NDArray
    */
   public abstract NDArray unitize();

   @Override
   public void updateVariables(@NonNull Consumer<Variable> updater) {
      throw new UnsupportedOperationException("NDArray does not support updating.");
   }

   /**
    * Zeros out the entries of the NDArray
    *
    * @return this NDArray
    */
   public NDArray zero() {
      return fill(0d);
   }

   /**
    * Creates a new NDArray with zero values with the same shape as this NDArray.
    *
    * @return the new zero valued NDArray
    */
   public abstract NDArray zeroLike();

   /**
    * Interface for testing two double values
    */
   @FunctionalInterface
   interface DoubleBinaryPredicate {

      /**
       * Tests a relation between two double values
       *
       * @param v1 the first value
       * @param v2 the second value
       * @return True if the predicate evaluates to True, False otherwise
       */
      boolean test(double v1, double v2);
   }

   /**
    * Interface for processing individual entries of an NDArray
    */
   @FunctionalInterface
   public interface EntryConsumer {

      /**
       * Consumes the value of the given index
       *
       * @param index the index
       * @param value the value
       */
      void apply(long index, double value);

   }

   public Sequence<?> decodeSequence(@NonNull Encoder encoder, @NonNull SequenceValidator validator) {
      VariableSequence sequence = new VariableSequence();
      String previous = "O";
      for (int word = 0; word < rows(); word++) {
         NDArray matrix = getRow(word);
         int l = (int) matrix.argmax();
         String tag = encoder.decode(l);
         while (!validator.isValid(tag, previous, matrix)) {
            matrix.set(l, Double.NEGATIVE_INFINITY);
            l = (int) matrix.argmax();
            tag = encoder.decode(l);
         }
         previous = tag;
         sequence.add(Variable.real(tag, matrix.get(l)));
      }
      return sequence;
   }

}//END OF NDArray
