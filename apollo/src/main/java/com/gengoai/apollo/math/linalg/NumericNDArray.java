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
import com.gengoai.apollo.data.observation.Sequence;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.data.observation.VariableSequence;
import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.math.Operator;
import com.gengoai.apollo.model.sequence.SequenceValidator;
import com.gengoai.conversion.Cast;
import com.gengoai.math.Operator;
import lombok.NonNull;
import org.apache.commons.math3.linear.RealMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

import static com.gengoai.Validation.checkArgument;

/**
 * <p>An NDArray whose elements are primitive numbers (e.g., float, double, int, long, etc.). Allows for manipulation
 * of elements and common vector/matrix/tensor operations to be performed on double representations of the numbers.</p>
 *
 * @author David B. Bracewell
 */
public abstract class NumericNDArray extends NDArray {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new NumericNDArray.
    *
    * @param shape the shape
    */
   public NumericNDArray(Shape shape) {
      super(shape);
   }

   @Override
   public NumericNDArray T() {
      return Cast.as(super.T());
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
    * }*
    * </pre>
    *
    * @param rhs the other NDArray whose values will be added
    * @return the new NDArray with the result of this + other
    */
   public NumericNDArray add(@NonNull NumericNDArray rhs) {
      return map(rhs, Operator::add);
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
    * }*
    * </pre>
    *
    * @param value the value to add
    * @return the new NDArray with the scalar value added
    */
   public NumericNDArray add(double value) {
      return map(value, Operator::add);
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
    * }*
    * </pre>
    *
    * @param axis the axis to add the given NDArray along
    * @param rhs  the NDArray to add
    * @return the resultant NDArray
    */
   public NumericNDArray add(int axis, @NonNull NumericNDArray rhs) {
      return map(axis, rhs, Operator::add);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the value of the axis to perform the add on
    * @param rhs      the NDArray to add
    * @return the resultant NDArray
    */
   public NumericNDArray add(int axis, int position, @NonNull NumericNDArray rhs) {
      return map(axis, position, rhs, Operator::add);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the position to add
    * @return the resultant NDArray
    */
   public NumericNDArray add(int axis, int position, @NonNull Number rhs) {
      return map(axis, position, rhs.doubleValue(), Operator::add);
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
    * }*
    * </pre>
    *
    * @param rhs the other NDArray whose values will be added
    * @return this NDArray with the result of this + rhs
    */
   public NumericNDArray addi(@NonNull NumericNDArray rhs) {
      return mapi(rhs, Operator::add);
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
    * }*
    * </pre>
    *
    * @param value the value to add
    * @return this NDArray with the scalar value added
    */
   public NumericNDArray addi(double value) {
      return mapi(value, Operator::add);
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
    * }*
    * </pre>
    *
    * @param axis the axis to add the given NDArray along
    * @param rhs  the NDArray to add
    * @return this NDArray with the results of the addition
    */
   public NumericNDArray addi(int axis, @NonNull NumericNDArray rhs) {
      return mapi(axis, rhs, Operator::add);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the NDArray to add
    * @return this NDArray with the results of the addition
    */
   public NumericNDArray addi(int axis, int position, @NonNull NumericNDArray rhs) {
      return mapi(axis, position, rhs, Operator::add);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the position to add
    * @return this NDArray with the results of the addition
    */
   public NumericNDArray addi(int axis, int position, @NonNull Number rhs) {
      return mapi(axis, position, rhs.doubleValue(), Operator::add);
   }

   @Override
   public NumericNDArray asNumericNDArray() {
      return this;
   }

   /**
    * <p>Wraps this NumericNDArray as a RealMatrix to be used with Apache Math.</p>
    *
    * @return the RealMatrix
    */
   public RealMatrix asRealMatrix() {
      return new RealMatrixWrapper(this);
   }

   @Override
   public NumericNDArray compact() {
      return Cast.as(super.compact());
   }

   @Override
   public NumericNDArray copy() {
      return Cast.as(super.copy());
   }

   /**
    * <p>Treats this NDArray as a sequence and decodes into a Sequence object using the given encoder and
    * validator/</p>
    *
    * @param encoder   the encoder to decode label indices into names
    * @param validator the validator to use for validating transitions between labels
    * @return the sequence
    */
   public Sequence<?> decodeSequence(@NonNull Encoder encoder,
                                     @NonNull SequenceValidator validator) {

      VariableSequence sequence = new VariableSequence();
      String previous = "O";
      for (int word = 0; word < shape().rows(); word++) {
         NumericNDArray matrix = getAxis(Shape.ROW, word);
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
    * }*
    * </pre>
    *
    * @param rhs the NDArray to divide this NDArry by
    * @return the new NDArray with the result of this / other
    */
   public NumericNDArray div(@NonNull NumericNDArray rhs) {
      return map(rhs, Operator::divide);
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
    * }*
    * </pre>
    *
    * @param value the value to divide this NDArray by
    * @return the new NDArray with the scalar value divided
    */
   public NumericNDArray div(double value) {
      return map(value, Operator::divide);
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
    * }*
    * </pre>
    *
    * @param axis the axis to divide the given NDArray along
    * @param rhs  the NDArray to divide by
    * @return the resultant NDArray
    */
   public NumericNDArray div(int axis, @NonNull NumericNDArray rhs) {
      return map(axis, rhs, Operator::divide);
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
    * }*
    * </pre>
    *
    * @param axis      the axis to divide the given NDArray along
    * @param axisValue the axisValue of the axis to perform the divide on
    * @param rhs       the NDArray to divide by
    * @return the resultant NDArray
    */
   public NumericNDArray div(int axis, int axisValue, @NonNull NumericNDArray rhs) {
      return map(axis, axisValue, rhs, Operator::divide);
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
    * }*
    * </pre>
    *
    * @param axis      the axis to divide the given NDArray along
    * @param axisValue the axisValue of the axis to perform the divide on
    * @param rhs       the axisValue to divide by
    * @return the resultant NDArray
    */
   public NumericNDArray div(int axis, int axisValue, @NonNull Number rhs) {
      return map(axis, axisValue, rhs.doubleValue(), Operator::divide);
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
    * }*
    * </pre>
    *
    * @param axis the axis to divide the given NDArray along
    * @param rhs  the NDArray to divide by
    * @return this NDArray with the results of the division
    */
   public NumericNDArray divi(int axis, @NonNull NumericNDArray rhs) {
      return mapi(axis, rhs, Operator::divide);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to divide the given NDArray along
    * @param position the position of the axis to perform the divide on
    * @param rhs      the NDArray to divide by
    * @return this NDArray with the results of the division
    */
   public NumericNDArray divi(int axis, int position, @NonNull NumericNDArray rhs) {
      return mapi(axis, position, rhs, Operator::divide);
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
    * }*
    * </pre>
    *
    * @param rhs the other NDArray whose values will be divided
    * @return this NDArray with the result of <code>this / rhs</code>
    */
   public NumericNDArray divi(@NonNull NumericNDArray rhs) {
      return mapi(rhs, Operator::divide);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to divide the given NDArray along
    * @param position the position of the axis to perform the divide on
    * @param rhs      the position to divide by
    * @return the resultant NDArray
    */
   public NumericNDArray divi(int axis, int position, @NonNull Number rhs) {
      return mapi(axis, position, rhs.doubleValue(), Operator::divide);
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
    * }*
    * </pre>
    *
    * @param value the value to divide
    * @return this NDArray with the scalar value divided
    */
   public NumericNDArray divi(double value) {
      return mapi(value, Operator::divide);
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
    * }*
    * </pre>
    *
    * @param rhs the NDArray to calculate the dot product with
    * @return NDArray of dot products
    */
   public double dot(@NonNull NumericNDArray rhs) {
      Validation.checkArgument(length() == rhs.length(), () -> "Length mismatch " + rhs.length() + " != " + length());
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
    * }*
    * </pre>
    *
    * @param rhs   the rhs
    * @param axis  the axis
    * @param other the other
    * @return the nd array
    */
   public NumericNDArray dot(@NonNull NumericNDArray rhs, int axis, int... other) {
      if (isEmpty() && rhs.isEmpty()) {
         return factory().empty();
      }
      if (shape().isScalar() && rhs.shape().isScalar()) {
         return factory().zeros(1).set(0, scalarDouble() * rhs.scalarDouble());
      }
      checkAxis(axis, this);
      for (int i : other) {
         checkAxis(i, this);
      }
      Shape comp = shape().remove(axis, other);
      NumericNDArray out = factory().zeros(comp);
      for (Index index : shape().range()) {
         double lv = getDouble(index);
         double rv = rhs.getDouble(rhs.shape().broadcast(index));
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
      if (obj instanceof NumericNDArray) {
         NumericNDArray r = Cast.as(obj);
         if (shape().equals(r.shape()) && getType() == r.getType()) {
            if (isEmpty() && r.isEmpty()) {
               return true;
            }
            return Arrays.equals(toFloatMatrix(), r.toFloatMatrix());
         }
      }
      return false;
   }

   @Override
   public NumericNDArrayFactory factory() {
      return Cast.as(super.factory());
   }

   /**
    * <p>Fills the NDArray with the given double value. (Only works on numeric NDArray).</p>
    *
    * @param value the value to set all cells in the NDArray
    * @return This NDArray
    */
   public NumericNDArray fill(double value) {
      for (int i = 0; i < length(); i++) {
         set(i, value);
      }
      return this;
   }

   @Override
   public NumericNDArray fill(@NonNull Object value) {
      checkArgument(value instanceof Number, () -> "Cannot fill NumericNDArray with '" + value.getClass()
                                                                                              .getSimpleName() + "' value.");
      Number n = Cast.as(value);
      for (long i = 0; i < length(); i++) {
         set(i, n);
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
   public NumericNDArray fillIf(@NonNull DoublePredicate test, double value) {
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
   public void forEachSparse(@NonNull NDArray.EntryConsumer<Number> consumer) {
      for (int i = 0; i < shape().length(); i++) {
         Number n = get(i);
         if (n.doubleValue() != 0) {
            consumer.apply(i, get(i));
         }
      }
   }

   @Override
   public Number get(long offset) {
      return get(shape().calculateIndex(offset));
   }

   @Override
   public Number get(int row, int col) {
      return get(0, 0, row, col);
   }

   @Override
   public Number get(int channel, int row, int col) {
      return get(0, channel, row, col);
   }

   @Override
   public abstract Number get(int kernel, int channel, int row, int col);

   @Override
   public Number get(@NonNull Index index) {
      return get(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn());
   }

   @Override
   public NumericNDArray get(@NonNull IndexRange range) {
      return Cast.as(super.get(range));
   }

   @Override
   public NumericNDArray getAxis(int axis, int position) {
      return Cast.as(super.getAxis(axis, position));
   }

   @Override
   public NumericNDArray getAxis(int axis, long[] positions) {
      return Cast.as(super.getAxis(axis, positions));
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
      return getDouble(shape().calculateIndex(index));
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
    * }*
    * </pre>
    *
    * @param axis     the axis to map
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NumericNDArray map(int axis,
                             @NonNull NumericNDArray rhs,
                             @NonNull DoubleBinaryOperator operator) {
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
    * }*
    * </pre>
    *
    * @param axis     the axis to map
    * @param position the dimension of the axis to be updated
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NumericNDArray map(int axis,
                             int position,
                             @NonNull NumericNDArray rhs,
                             @NonNull DoubleBinaryOperator operator) {
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
    * }*
    * </pre>
    *
    * @param axis        the axis to map
    * @param position    the dimension of the axis to be updated
    * @param doubleValue the doubleValue
    * @param operator    the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NumericNDArray map(int axis,
                             int position,
                             double doubleValue,
                             @NonNull DoubleBinaryOperator operator) {
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
    * }*
    * </pre>
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return the transformed NDArray
    */
   public NumericNDArray map(@NonNull DoubleUnaryOperator operator) {
      NumericNDArray out = zeroLike();
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
    * }*
    * </pre>
    *
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NumericNDArray map(@NonNull NumericNDArray rhs,
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
    * }*
    * </pre>
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NumericNDArray map(double value,
                             @NonNull DoubleBinaryOperator operator) {
      return NDArrayOps.mapDouble(this, value, operator, null);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to map
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NumericNDArray mapi(int axis,
                              @NonNull NumericNDArray rhs,
                              @NonNull DoubleBinaryOperator operator) {

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
    * }*
    * </pre>
    *
    * @param axis     the row whose values we want to manipulate
    * @param position the position of the axis to be updated
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NumericNDArray mapi(int axis,
                              int position,
                              double value,
                              @NonNull DoubleBinaryOperator operator) {
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
    * }*
    * </pre>
    *
    * @param axis     the row whose values we want to manipulate
    * @param position the position of the axis to be updated
    * @param rhs      the right hand side NDArray
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NumericNDArray mapi(int axis,
                              int position,
                              @NonNull NumericNDArray rhs,
                              @NonNull DoubleBinaryOperator operator) {
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
    * }*
    * </pre>
    *
    * @param operator the operation to perform on the values of this NDArray
    * @return this NDArray with the operator applied
    */
   public NumericNDArray mapi(@NonNull DoubleUnaryOperator operator) {

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
    * }*
    * </pre>
    *
    * @param rhs      the rhs
    * @param operator the operation to perform on the values of this NDArray and the given NDArray
    * @return the transformed NDArray
    */
   public NumericNDArray mapi(@NonNull NumericNDArray rhs, @NonNull DoubleBinaryOperator operator) {

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
    * }*
    * </pre>
    *
    * @param value    the value
    * @param operator the operation to perform on the values of this NDArray and the given value
    * @return the transformed NDArray
    */
   public NumericNDArray mapi(double value, @NonNull DoubleBinaryOperator operator) {

      return NDArrayOps.mapDouble(this, value, operator, this);
   }

   /**
    * Matrix multiplication numeric nd array.
    *
    * @param rhs the rhs
    * @return the numeric nd array
    */
   protected NumericNDArray matrixMultiplicationImpl(NumericNDArray rhs) {
      checkArgument(shape().columns() == rhs.shape().rows(),
                    () -> "Cannot multiply NDArray of shape " + shape() + " by NDArray of shape " + rhs
                          .shape());
      NumericNDArray out = factory().zeros(Shape.shape(shape().rows(), rhs.shape().columns()));
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

   @Override
   public Number max() {
      return Cast.as(super.max());
   }

   @Override
   public NumericNDArray max(int axis, @NonNull int... other) {
      return Cast.as(super.max(axis, other));
   }

   /**
    * <p>Calculates the mean value in the NDArray across all slices. (Only supported by numeric NDArray).</p>
    *
    * @return the mean value
    */
   public double mean() {

      return sum() / length();
   }

   /**
    * <p>Calculates the mean values along the given axis across slices in the NDArray. (Only supported by numeric
    * NDArray).</p>
    *
    * @param axis  the axis
    * @param other the other
    * @return the mean value
    */
   public NumericNDArray mean(int axis, @NonNull int... other) {
      return NDArrayOps.reduceDoubleAxis(this, nd.DFLOAT32, Operator::add, axis, other)
                       .divi(IntStream.concat(IntStream.of(axis), IntStream.of(other))
                                      .map(shape()::get)
                                      .distinct()
                                      .reduce(1, Operator::multiply));
   }

   @Override
   public Number min() {
      return Cast.as(super.min());
   }

   @Override
   public NumericNDArray min(int axis, int... other) {
      return Cast.as(super.min(axis, other));
   }

   /**
    * <p>Creates a new NDArray by multiplying the (matrix) slices of this NDArray with those in the given NDArray.
    * (Only supported by numeric NDArray).</p>
    *
    * @param rhs the NDArray to multiply
    * @return the resulting NDArray
    */
   public NumericNDArray mmul(@NonNull NumericNDArray rhs) {
      checkArgument(shape().columns() == rhs.shape().rows(),
                    () -> "Cannot multiply NDArray of shape " +
                          shape() +
                          " by NDArray of shape " +
                          rhs.shape());
      if (shape().isVector() || shape().isMatrix()) {
         return matrixMultiplicationImpl(rhs);
      }
      NumericNDArray out = factory().zeros(shape().with(Shape.COLUMN, rhs.shape().columns()));
      for (int i = 0; i < shape().sliceLength(); i++) {
         int rslice = rhs.shape().sliceLength() == 1 ? 0 : i;
         out.setSlice(i, slice(i).mmul(rhs.slice(rslice)));
      }
      return out;
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
    * }*
    * </pre>
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return the new NDArray with the result of <code>this * rhs</code>
    */
   public NumericNDArray mul(@NonNull NumericNDArray rhs) {
      return map(rhs, Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param value the value to multiply
    * @return the new NDArray with the scalar value multiply
    */
   public NumericNDArray mul(double value) {
      return map(value, Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param axis the axis to multiply the given NDArray along
    * @param rhs  the NDArray to multiply
    * @return the resultant NDArray
    */
   public NumericNDArray mul(int axis, @NonNull NumericNDArray rhs) {
      return map(axis, rhs, Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param axis      the axis to multiply the given NDArray along
    * @param axisValue the axisValue of the axis to perform the multiply on
    * @param rhs       the NDArray to multiply
    * @return the resultant NDArray
    */
   public NumericNDArray mul(int axis, int axisValue, @NonNull NumericNDArray rhs) {
      return map(axis, axisValue, rhs, Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param axis      the axis to multiply the given NDArray along
    * @param axisValue the axisValue of the axis to perform the multiply on
    * @param rhs       the NDArray to multiply
    * @return the resultant NDArray
    */
   public NumericNDArray mul(int axis, int axisValue, @NonNull Number rhs) {
      return map(axis, axisValue, rhs.doubleValue(), Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param rhs the other NDArray whose values will be multiplied
    * @return this NDArray
    */
   public NumericNDArray muli(@NonNull NumericNDArray rhs) {
      return mapi(rhs, Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param value the value to multiply
    * @return this NDArray
    */
   public NumericNDArray muli(double value) {
      return mapi(value, Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param axis the axis to multiply the given NDArray along
    * @param rhs  the NDArray to multiply
    * @return this NDArray
    */
   public NumericNDArray muli(int axis, @NonNull NumericNDArray rhs) {
      return mapi(axis, rhs, Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the NDArray to add
    * @return this NDArray with the results of the addition
    */
   public NumericNDArray muli(int axis, int position, @NonNull NumericNDArray rhs) {
      return mapi(axis, position, rhs, Operator::multiply);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to add the given NDArray along
    * @param position the position of the axis to perform the add on
    * @param rhs      the NDArray to add
    * @return this NDArray with the results of the addition
    */
   public NumericNDArray muli(int axis, int position, Number rhs) {
      return mapi(axis, position, rhs.doubleValue(), Operator::multiply);
   }

   /**
    * <p>Calculates the norm1 of the values along the given axis in the NDArray across slices. (Only supported by
    * numeric NDArray).</p>
    *
    * @param axis  the axis
    * @param other the other
    * @return the L1 norm value
    */
   public NumericNDArray norm1(int axis, @NonNull int... other) {

      if (rank() <= 1) {
         return NDArrayOps.reduceDoubleAxis(this, (a, b) -> a + Math.abs(b), axis, other);
      }
      int[] f = IntStream.concat(IntStream.of(axis), IntStream.of(other))
                         .distinct()
                         .sorted()
                         .toArray();
      NumericNDArray sum = map(Math::abs).sum(f[0]);
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
    * @param axis  the axis
    * @param other the other
    * @return the L2 norm value
    */
   public NumericNDArray norm2(int axis, int... other) {

      if (shape().rank() <= 1) {
         return NDArrayOps.reduceDoubleAxis(this, (a, b) -> a + b * b, axis, other).mapi(Math::sqrt);
      }
      NumericNDArray n = map(a -> a * a);
      return NDArrayOps.reduceDoubleAxis(n, Operator::add, axis, other).mapi(Math::sqrt);
   }

   /**
    * <p>Calculates the L2 norm -- or Magnitude -- of the NDArray across slices. (Only support numeric NDArray)</p>
    *
    * @return the L2 norm of the NDArray
    */
   public double norm2() {

      double l2 = 0;
      for (Index index : shape().range()) {
         double v = getDouble(index);
         l2 += (v * v);
      }
      return Math.sqrt(l2);
   }


   @Override
   public NumericNDArray padPost(int axis, int length) {
      return padPost(shape().with(axis, length));
   }

   @Override
   public NumericNDArray padPost(@NonNull int... axisLengthPairs) {
      return padPost(shape().with(axisLengthPairs));
   }

   @Override
   public NumericNDArray padPost(@NonNull Shape paddedShape) {
      NumericNDArray out = factory().zeros(paddedShape);
      for (Index index : shape().range()) {
         if (paddedShape.contains(index)) {
            out.set(index, get(index));
         }
      }
      return out;
   }

   /**
    * <p>Constructs a new NDArray where the given <code>axis</code> is changed to have <code>length</code> values,
    * where this NDArray will either be padded with the given <code>padValue</code> to extend its size to the new length
    * or truncated to match the new length. All padding is done at the end of the axis.</p>
    *
    * @param padValue the padding value
    * @param axis     the axis to pad.
    * @param length   the new length of the given axis
    * @return the padded NDArray.
    */
   public NumericNDArray padPost(double padValue, int axis, int length) {
      return padPost(padValue, shape().with(axis, length));
   }

   /**
    * <p>Constructs a new NDArray where the given <code>axes</code> are changed to have the given <code>length</code>
    * values, where this NDArray will either be padded with the given <code>padValue</code> to extend its size to the
    * new lengths or truncated to match the new lengths. All padding is done at the end of the axis. Note that the
    * argument to this method expects (int, int) pairs where the first integer is the axis and the second the new
    * length.</p>
    *
    * @param padValue        the padding value
    * @param axisLengthPairs array of integers <code>axis1, length, axis2, length2, ... ,axisN, lengthN</code>
    * @return the padded NDArray.
    */
   public NumericNDArray padPost(double padValue, @NonNull int... axisLengthPairs) {
      return padPost(padValue, shape().with(axisLengthPairs));
   }

   /**
    * <p>Constructs a new NDArray where  where this NDArray will either be padded with the given <code>padValue</code>
    * to extend its size to the new length or truncated to the given Shape. All padding is done at the end of the
    * axis.</p>
    *
    * @param padValue    the padding value
    * @param paddedShape the shape of the padded NDArray
    * @return the padded NDArray.
    */
   public NumericNDArray padPost(double padValue, @NonNull Shape paddedShape) {
      NumericNDArray out = factory().create(paddedShape, NDArrayInitializer.constant(padValue));
      for (Index index : shape().range()) {
         if (paddedShape.contains(index)) {
            out.set(index, get(index));
         }
      }
      return out;
   }

   @Override
   public NumericNDArray padPostWith(@NonNull Object padValue, @NonNull Shape paddedShape){
      Validation.checkArgument(padValue instanceof Number, "Invalid padValue type '" + padValue.getClass().getSimpleName() + "'");
      return padPost(Cast.as(padValue,Number.class).doubleValue(), paddedShape);
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
    * }*
    * </pre>
    *
    * @param lhs the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rdiv(@NonNull NumericNDArray lhs) {
      return map(lhs, (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param lhs the left hand side value for division
    * @return the new resultant NDArray
    */
   public NumericNDArray rdiv(double lhs) {
      return map(lhs, (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param axis the axis
    * @param lhs  the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rdiv(int axis, @NonNull NumericNDArray lhs) {
      return map(axis, lhs, (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param axis     the axis
    * @param position the position
    * @param lhs      the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rdiv(int axis, int position, @NonNull NumericNDArray lhs) {
      return map(axis, position, lhs, (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param axis     the axis
    * @param position the position
    * @param lhs      the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rdiv(int axis, int position, @NonNull Number lhs) {
      return map(axis, position, lhs.doubleValue(), (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param lhs the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rdivi(@NonNull NumericNDArray lhs) {
      return mapi(lhs, (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param lhs the left hand side value for division
    * @return the new resultant NDArray
    */
   public NumericNDArray rdivi(double lhs) {
      return mapi(lhs, (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param axis the axis to perform the operation along
    * @param lhs  the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rdivi(int axis, @NonNull NumericNDArray lhs) {
      return mapi(axis, lhs, (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to perform the operation along
    * @param position the position of the axis to restrict the operation on
    * @param lhs      the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rdivi(int axis, int position, @NonNull NumericNDArray lhs) {
      return mapi(axis, position, lhs, (a, b) -> b / a);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to perform the operation along
    * @param position the position of the axis to restrict the operation on
    * @param lhs      the left hand side NDArray of the division operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rdivi(int axis, int position, @NonNull Number lhs) {
      return mapi(axis, position, lhs.doubleValue(), (a, b) -> b / a);
   }

   @Override
   public abstract NumericNDArray reshape(@NonNull Shape newShape);

   @Override
   public NumericNDArray reshape(@NonNull int... dims) {
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
    * }*
    * </pre>
    *
    * @param lhs the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rsub(@NonNull NumericNDArray lhs) {
      return map(lhs, (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param lhs the left hand side value for subtraction
    * @return the new resultant NDArray
    */
   public NumericNDArray rsub(double lhs) {
      return map(lhs, (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param axis the axis
    * @param lhs  the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rsub(int axis, @NonNull NumericNDArray lhs) {
      return map(axis, lhs, (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param axis     the axis
    * @param position the position
    * @param lhs      the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rsub(int axis, int position, @NonNull NumericNDArray lhs) {
      return map(axis, position, lhs, (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param axis     the axis
    * @param position the position
    * @param lhs      the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rsub(int axis, int position, @NonNull Number lhs) {
      return map(axis, position, lhs.doubleValue(), (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param axis the axis to perform the operation along
    * @param lhs  the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rsubi(int axis, @NonNull NumericNDArray lhs) {
      return mapi(axis, lhs, (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to perform the operation along
    * @param position the position of the axis to restrict the operation on
    * @param lhs      the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rsubi(int axis, int position, @NonNull NumericNDArray lhs) {
      return mapi(axis, position, lhs, (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to perform the operation along
    * @param position the position of the axis to restrict the operation on
    * @param lhs      the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rsubi(int axis, int position, @NonNull Number lhs) {
      return mapi(axis, position, lhs.doubleValue(), (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param lhs the left hand side NDArray of the subtraction operator.
    * @return the new resultant NDArray
    */
   public NumericNDArray rsubi(@NonNull NumericNDArray lhs) {
      return mapi(lhs, (a, b) -> b - a);
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
    * }*
    * </pre>
    *
    * @param lhs the left hand side value for division
    * @return the new resultant NDArray
    */
   public NumericNDArray rsubi(double lhs) {
      return mapi(lhs, (a, b) -> b - a);
   }

   /**
    * <p>Returns the scalar value of this NDArray (value at <code>(0,0,0,0)</code>)</p>
    *
    * @return the scalar value
    */
   public double scalarDouble() {
      return getDouble(0);
   }

   /**
    * <p>Sets the value of the element at the given index. (row/column if vector, entry if other)</p>
    *
    * @param index the index
    * @param value the value
    * @return this NDArray
    */
   public NumericNDArray set(long index, double value) {
      return set(shape().calculateIndex(index), value);
   }

   /**
    * <p>Sets the value of the element at the given row and column (assumes kernel and channel are 0).</p>
    *
    * @param row   the row index
    * @param col   the column index
    * @param value the value
    * @return this NDArray
    */
   public NumericNDArray set(int row, int col, double value) {
      return set(0, 0, row, col, value);
   }

   /**
    * <p>Sets the value of the element at the given row and column (assumes kernel and channel are 0).</p>
    *
    * @param channel the channel
    * @param row     the row index
    * @param col     the column index
    * @param value   the value
    * @return this NDArray
    */
   public NumericNDArray set(int channel, int row, int col, double value) {
      return set(0, channel, row, col, value);
   }

   /**
    * <p>Sets the value of the element at the given row and column (assumes kernel and channel are 0).</p>
    *
    * @param kernel  the kernel
    * @param channel the channel
    * @param row     the row index
    * @param col     the column index
    * @param value   the value
    * @return this NDArray
    */
   public abstract NumericNDArray set(int kernel, int channel, int row, int col, double value);

   /**
    * <p>Sets the value of the element at the given index. (row/column if vector, entry if other)</p>
    *
    * @param index the index
    * @param value the value
    * @return this NDArray
    */
   public NumericNDArray set(@NonNull Index index, double value) {
      return set(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn(), value);
   }

   @Override
   public NumericNDArray set(long index, Object value) {
      return set(shape().calculateIndex(index), value);
   }

   @Override
   public NumericNDArray set(int row, int col, @NonNull Object value) {
      return set(0, 0, row, col, value);
   }

   @Override
   public NumericNDArray set(int channel, int row, int col, @NonNull Object value) {
      return set(0, channel, row, col, value);
   }

   @Override
   public abstract NumericNDArray set(int kernel, int channel, int row, int col, @NonNull Object value);

   @Override
   public NumericNDArray set(@NonNull Index index, @NonNull Object value) {
      return set(index.getKernel(), index.getChannel(), index.getRow(), index.getColumn(), value);
   }

   @Override
   public NumericNDArray setAxis(int axis, int position, @NonNull NDArray rhs) {
      return Cast.as(super.setAxis(axis, position, rhs));
   }

   @Override
   public NumericNDArray setAxis(int axis, int position, Object rhs) {
      return Cast.as(super.setAxis(axis, position, rhs));
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
   public NumericNDArray setAxisDouble(int axis, int position, @NonNull NumericNDArray rhs) {
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
   public NumericNDArray setAxisDouble(int axis, int position, double rhs) {
      return setRangeDouble(shape().iterateAlong(axis, position), rhs);
   }

   @Override
   public NumericNDArray setLabel(Object label) {
      return Cast.as(super.setLabel(label));
   }

   @Override
   public NumericNDArray setPredicted(Object predicted) {
      return Cast.as(super.setPredicted(predicted));
   }

   @Override
   public NumericNDArray setRange(@NonNull IndexRange indexRange, @NonNull NDArray rhs) {
      return Cast.as(super.setRange(indexRange, rhs));
   }

   @Override
   public NumericNDArray setRange(@NonNull IndexRange indexRange, Object rhs) {
      return Cast.as(super.setRange(indexRange, rhs));
   }

   /**
    * <p>Sets the range <code>[from, to)</code> of values in this NDArray to those of the given <code>rhs</code>. The
    * given values NDArray will be broadcast as necessary.</p>
    *
    * @param indexRange The range of indices to use for setting the values from the given NDArray
    * @param rhs        the NDArray whose values we will assign to this NDArray.
    * @return this NDArray
    */
   public NumericNDArray setRangeDouble(@NonNull IndexRange indexRange,
                                        @NonNull NumericNDArray rhs) {
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
   public NumericNDArray setRangeDouble(@NonNull IndexRange indexRange, double rhs) {
      indexRange.forEach(i -> set(i, rhs));
      return this;
   }

   @Override
   public NumericNDArray setSlice(int index, @NonNull NDArray slice) {
      return Cast.as(super.setSlice(index, slice));
   }

   @Override
   public NumericNDArray setSlice(int kernel, int channel, @NonNull NDArray slice) {
      return setSlice(shape().calculateSliceIndex(kernel, channel), slice);
   }

   @Override
   public NumericNDArray setWeight(double weight) {
      return Cast.as(super.setWeight(weight));
   }

   @Override
   public abstract NumericNDArray slice(int index);

   @Override
   public abstract NumericNDArray slice(int startKernel, int startChannel, int endKernel, int endChannel);

   @Override
   public NumericNDArray slice(int kernel, int channel) {
      return slice(shape().calculateSliceIndex(kernel, channel));
   }

   @Override
   public NumericNDArray slice(@NonNull Index index) {
      return slice(shape().calculateSliceIndex(index));
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
    * }*
    * </pre>
    *
    * @param rhs the other NDArray whose values will be subtract
    * @return the new NDArray with the result of <code>this - other</code>
    */
   public NumericNDArray sub(@NonNull NumericNDArray rhs) {
      return map(rhs, Operator::subtract);
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
    * }*
    * </pre>
    *
    * @param value the value to subtracted
    * @return the new NDArray with the scalar value subtracted
    */
   public NumericNDArray sub(double value) {
      return map(value, Operator::subtract);
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
    * }*
    * </pre>
    *
    * @param axis the axis to subtract the given NDArray along
    * @param rhs  the NDArray to subtract
    * @return the resultant NDArray
    */
   public NumericNDArray sub(int axis, @NonNull NumericNDArray rhs) {
      return map(axis, rhs, Operator::subtract);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to subtract the given NDArray along
    * @param position the position of the axis to perform the subtract on
    * @param rhs      the NDArray to subtract
    * @return the resultant NDArray
    */
   public NumericNDArray sub(int axis, int position, @NonNull NumericNDArray rhs) {
      return map(axis, position, rhs, Operator::subtract);
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
    *   b = a.add(Shape.ROW, 0,  2}*);
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
   public NumericNDArray sub(int axis, int position, @NonNull Number rhs) {
      return map(axis, position, rhs.doubleValue(), Operator::subtract);
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
    * }*
    * </pre>
    *
    * @param rhs the other NDArray whose values will be subtracted
    * @return this NDArray with the result of <code>this - rhs</code>
    */
   public NumericNDArray subi(@NonNull NumericNDArray rhs) {
      return mapi(rhs, Operator::subtract);
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
    * }*
    * </pre>
    *
    * @param value the value to subtract
    * @return this NDArray with the scalar value subtracted
    */
   public NumericNDArray subi(double value) {
      return mapi(value, Operator::subtract);
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
    * }*
    * </pre>
    *
    * @param axis the axis to subtract the given NDArray along
    * @param rhs  the NDArray to subtract
    * @return this NDArray with the results of the subtraction
    */
   public NumericNDArray subi(int axis, @NonNull NumericNDArray rhs) {
      return mapi(axis, rhs, Operator::subtract);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to subtract the given NDArray along
    * @param position the position of the axis to perform the subtract on
    * @param rhs      the NDArray to subtract
    * @return this NDArray with the results of the subtraction
    */
   public NumericNDArray subi(int axis, int position, @NonNull NumericNDArray rhs) {
      return mapi(axis, position, rhs, Operator::subtract);
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
    * }*
    * </pre>
    *
    * @param axis     the axis to subtract the given NDArray along
    * @param position the position of the axis to perform the subtract on
    * @param rhs      the number to subtract
    * @return this NDArray with the results of the subtraction
    */
   public NumericNDArray subi(int axis, int position, @NonNull Number rhs) {
      return mapi(axis, position, rhs.doubleValue(), Operator::subtract);
   }

   /**
    * <p>Calculates the sum of values along the given axis in the NDArray across slices. (Only supported by numeric
    * NDArray).</p>
    *
    * @param axis  the axis
    * @param other the other
    * @return the sumb value
    */
   public NumericNDArray sum(int axis, int... other) {
      return NDArrayOps.reduceDoubleAxis(this, Operator::add, axis, other);
   }

   /**
    * <p>Calculates the sum of all values in the NDArray across slices. (Only supported by numeric NDArrays)</>
    *
    * @return the sum
    */
   public double sum() {
      return shape()
            .range()
            .stream()
            .mapToDouble(this::getDouble).reduce(0, Operator::add);
   }

   /**
    * <p>Calculates the sum of squares of the values along the given axis in the NDArray across slices. (Only supported
    * by numeric NDArray).</p>
    *
    * @param axis  the axis
    * @param other the other
    * @return the sumb value
    */
   public NumericNDArray sumOfSquares(int axis, int... other) {
      return NDArrayOps.reduceDoubleAxis(this, (a, b) -> a + (b * b), axis, other);
   }

   /**
    * <p>Calculates the sum of squares for all values in the NDArray across slices. (Only supported by numeric
    * NDArrays)</p>
    *
    * @return the sum of squares
    */
   public double sumOfSquares() {
      return shape()
            .range()
            .stream()
            .mapToDouble(this::getDouble).reduce(0, (a, b) -> a + (b * b));
   }

   /**
    * <p>Converts this NDArray into a double array. The array is encoded using column major order for matrices and
    * kernel-major order for slices.</p>
    *
    * @return the double array
    */
   public double[] toDoubleArray() {
      double[] out = new double[(int) length()];
      for (long i = 0; i < length(); i++) {
         out[(int) i] = getDouble(i);
      }
      return out;
   }

   /**
    * <p>Converts this NDArray into a float array. The array is encoded using column major order for matrices and
    * kernel-major order for slices.</p>
    *
    * @return the float array
    */
   public float[] toFloatArray() {
      float[] out = new float[(int) length()];
      for (long i = 0; i < length(); i++) {
         out[(int) i] = (float)getDouble(i);
      }
      return out;
   }

   /**
    * <p>Converts this NDArray into a long array. The array is encoded using column major order for matrices and
    * kernel-major order for slices.</p>
    *
    * @return the long array
    */
   public long[] toLongArray() {
      long[] out = new long[(int) length()];
      for (long i = 0; i < length(); i++) {
         out[(int) i] = get(i).longValue();
      }
      return out;
   }

   /**
    * <p>Converts this NDArray into a int array. The array is encoded using column major order for matrices and
    * kernel-major order for slices.</p>
    *
    * @return the int array
    */
   public int[] toIntArray() {
      int[] out = new int[(int) length()];
      for (long i = 0; i < length(); i++) {
         out[(int) i] = (int)getDouble(i);
      }
      return out;
   }

   /**
    * <p>Converts the NDArray into an array of DoubleMatrix. (one per slice)</p>
    *
    * @return the array of DoubleMatrix
    */
   public DoubleMatrix[] toDoubleMatrix() {
      if (shape().isEmpty()) {
         return new DoubleMatrix[0];
      }
      if (shape().isScalar()) {
         return new DoubleMatrix[]{DoubleMatrix.scalar(scalarDouble())};
      }
      DoubleMatrix[] m = new DoubleMatrix[shape().sliceLength()];
      for (int i = 0; i < shape().sliceLength(); i++) {
         DoubleMatrix v = new DoubleMatrix(Math.max(1, shape().rows()), shape().columns());
         NumericNDArray n = slice(i);
         for (int j = 0; j < n.length(); j++) {
            v.put(j, n.getDouble(j));
         }
         m[i] = v;
      }
      return m;
   }

   /**
    * <p>Converts the NDArray into an array of FloatMatrix. (one per slice)</p>
    *
    * @return the array of FloatMatrix
    */
   public FloatMatrix[] toFloatMatrix() {
      if (shape().isEmpty()) {
         return new FloatMatrix[0];
      }
      if (shape().isScalar()) {
         return new FloatMatrix[]{FloatMatrix.scalar((float) scalarDouble())};
      }
      FloatMatrix[] m = new FloatMatrix[shape().sliceLength()];
      for (int i = 0; i < shape().sliceLength(); i++) {
         FloatMatrix v = new FloatMatrix(Math.max(1, shape().rows()), shape().columns());
         NumericNDArray n = slice(i);
         for (int j = 0; j < n.length(); j++) {
            v.put(j, (float) n.getDouble(j));
         }
         m[i] = v;
      }
      return m;
   }

   @Override
   public NumericNDArray transpose(@NonNull int... newAxes) {
      return Cast.as(super.transpose(newAxes));
   }

   /**
    * <p>Unitizes the NDArray by dividing the values by L2 Norm. (Only supported by numeric NDArrays)</p>
    *
    * @return Unitized version of this NDArray
    */
   public NumericNDArray unitize() {
      NumericNDArray out = nd.DFLOAT32.zeros(shape());
      double norm2 = norm2();
      shape().range().forEach(ii -> out.set(ii, getDouble(ii) / norm2));
      return out;
   }

   @Override
   public NumericNDArray zero() {
      return fill(0d);
   }

   @Override
   public NumericNDArray zeroLike() {
      return factory().zeros(shape());
   }
}//END OF NumericNDArray
