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

import com.gengoai.math.Operator;
import lombok.NonNull;

public abstract class NumericNDArray extends NDArray {

   public NumericNDArray(Shape shape) {
      super(shape);
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
   public NumericNDArray div(@NonNull NumericNDArray rhs) {
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
   public NumericNDArray div(double value) {
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
   public NumericNDArray div(int axis, @NonNull NumericNDArray rhs) {
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
   public NumericNDArray div(int axis, int axisValue, @NonNull NumericNDArray rhs) {
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
   public NumericNDArray div(int axis, int axisValue, @NonNull Number rhs) {
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
   public NumericNDArray divi(int axis, @NonNull NumericNDArray rhs) {
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
   public NumericNDArray divi(int axis, int position, @NonNull NumericNDArray rhs) {
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
   public NumericNDArray divi(@NonNull NumericNDArray rhs) {
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
   public NumericNDArray divi(int axis, int position, @NonNull Number rhs) {
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
   public NumericNDArray divi(double value) {
      return mapiDouble(value, Operator::divide);
   }

}
