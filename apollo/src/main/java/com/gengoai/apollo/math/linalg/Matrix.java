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

import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import lombok.NonNull;
import org.apache.mahout.math.list.IntArrayList;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.IntStream;

/**
 * Specialized NDArray for vectors and matrices.
 *
 * @author David B. Bracewell
 */
public abstract class Matrix extends NDArray {
   private static final long serialVersionUID = 1L;

   /**
    * Checks that the length of the second shape is equal to the given dimension.
    *
    * @param dim the dimension to check
    * @param s2  the shape to check
    */
   protected static void checkLength(int dim, Shape s2) {
      if(s2.sliceLength > 1) {
         throw new IllegalArgumentException("Slice Mismatch: " + s2.sliceLength + " != 1");
      }
      if(dim != s2.matrixLength) {
         throw new IllegalArgumentException("Length Mismatch: " + s2.matrixLength + " != " + dim);
      }
   }

   protected Matrix(@NonNull Shape shape) {
      super(shape);
   }

   @Override
   public long argmax() {
      long index = -1;
      double max = Double.NEGATIVE_INFINITY;
      for(int i = 0; i < shape.matrixLength; i++) {
         double v = get(i);
         if(v > max) {
            max = v;
            index = i;
         }
      }
      return index;
   }

   @Override
   public long argmin() {
      long index = -1;
      double min = Double.POSITIVE_INFINITY;
      for(int i = 0; i < shape.matrixLength; i++) {
         double v = get(i);
         if(v < min) {
            min = v;
            index = i;
         }
      }
      return index;
   }

   /**
    * Checks that the two shapes have the same length.
    *
    * @param s2 the second shape
    */
   protected void checkLength(Shape s2) {
      if(s2.sliceLength > 1) {
         throw new IllegalArgumentException("Slice Mismatch: " + s2.sliceLength + " != 1");
      }
      if(shape().matrixLength != s2.matrixLength) {
         throw new IllegalArgumentException("Length Mismatch: " + shape().matrixLength + " != " + s2.matrixLength);
      }
   }

   @Override
   public NDArray columnArgmaxs() {
      NDArray array = new DenseMatrix(1, shape.columns());
      for(int c = 0; c < shape.columns(); c++) {
         double max = Double.NEGATIVE_INFINITY;
         int index = -1;
         for(int r = 0; r < shape.rows(); r++) {
            double v = get(r, c);
            if(v > max) {
               max = v;
               index = r;
            }
         }
         array.set(c, index);
      }
      return array;
   }

   @Override
   public NDArray columnArgmins() {
      NDArray array = new DenseMatrix(1, shape.columns());
      for(int c = 0; c < shape.columns(); c++) {
         double min = Double.POSITIVE_INFINITY;
         int index = -1;
         for(int r = 0; r < shape.rows(); r++) {
            double v = get(r, c);
            if(v < min) {
               min = v;
               index = r;
            }
         }
         array.set(c, index);
      }
      return array;
   }

   @Override
   public NDArray columnMaxs() {
      NDArray array = new DenseMatrix(1, shape.columns());
      for(int c = 0; c < shape.columns(); c++) {
         double max = Double.NEGATIVE_INFINITY;
         for(int r = 0; r < shape.rows(); r++) {
            max = Math.max(max, get(r, c));
         }
         array.set(c, max);
      }
      return array;
   }

   @Override
   public NDArray columnMins() {
      NDArray array = new DenseMatrix(1, shape.columns());
      for(int c = 0; c < shape.columns(); c++) {
         double min = Double.POSITIVE_INFINITY;
         for(int r = 0; r < shape.rows(); r++) {
            min = Math.min(min, get(r, c));
         }
         array.set(c, min);
      }
      return array;
   }

   @Override
   public NDArray columnSums() {
      NDArray array = new DenseMatrix(1, shape.columns());
      for(int c = 0; c < shape.columns(); c++) {
         double sum = 0;
         for(int r = 0; r < shape.rows(); r++) {
            sum += get(r, c);
         }
         array.set(c, sum);
      }
      return array;
   }

   @Override
   public NDArray diag() {
      if(shape.isScalar()) {
         return copy();
      }

      if(shape.isRowVector()) {
         NDArray out = NDArrayFactory.ND.array(shape.columns(), shape.columns());
         for(int i = 0; i < shape.columns(); i++) {
            out.set(i, i, get(i));
         }
         return out;
      }

      if(shape.isColumnVector()) {
         NDArray out = NDArrayFactory.ND.array(shape.rows(), shape.rows());
         for(int i = 0; i < shape.rows(); i++) {
            out.set(i, i, get(i));
         }
         return out;
      }

      if(shape.isSquare()) {
         NDArray out = zeroLike();
         for(int r = 0; r < shape.rows(); r++) {
            if(r < shape.columns()) {
               out.set(r, r, get(r, r));
            }
         }
         return out;
      }

      throw new IllegalStateException("Rectangular slices are not supported");
   }

   @Override
   public boolean equals(Object o) {
      if(o instanceof Matrix) {
         return Arrays.equals(toDoubleArray(), Cast.<Matrix>as(o).toDoubleArray());
      }
      return false;
   }

   @Override
   public NDArray fill(double value) {
      if(value == 0) {
         return zero();
      }
      for(int i = 0; i < shape.matrixLength; i++) {
         set(i, value);
      }
      return this;
   }

   @Override
   public double get(int channel, int row, int col) {
      if(channel == 0) {
         return get(row, col);
      }
      throw new IndexOutOfBoundsException();
   }

   @Override
   public double get(int kernel, int channel, int row, int col) {
      if(channel == 0 && kernel == 0) {
         return get(row, col);
      }
      throw new IndexOutOfBoundsException();
   }

   @Override
   public NDArray getColumns(int[] columns) {
      columns = IntStream.of(columns).distinct().sorted().toArray();
      NDArray out = NDArrayFactory.ND.array(shape.rows(), columns.length);
      for(int i = 0; i < columns.length; i++) {
         out.setColumn(i, getColumn(columns[i]));
      }
      return out;
   }

   @Override
   public NDArray getColumns(int from, int to) {
      NDArray out = NDArrayFactory.ND.array(shape.rows(), (to - from));
      int index = 0;
      for(int i = from; i < to; i++) {
         out.setColumn(index, getColumn(i));
         index++;
      }
      return out;
   }

   @Override
   public NDArray getRows(int[] rows) {
      rows = IntStream.of(rows).distinct().sorted().toArray();
      NDArray out = NDArrayFactory.ND.array(rows.length, shape.columns());
      for(int i = 0; i < rows.length; i++) {
         out.setRow(i, getRow(rows[i]));
      }
      return out;
   }

   @Override
   public NDArray getRows(int from, int to) {
      NDArray out = NDArrayFactory.ND.array((to - from), shape.columns());
      int index = 0;
      for(int i = from; i < to; i++) {
         out.setRow(index, getRow(i));
         index++;
      }
      return out;
   }

   @Override
   public NDArray incrementiColumn(int c, @NonNull NDArray inc) {
      Validation.checkArgument(rows() == inc.length(), "Invalid Row size");
      for(int r = 0; r < rows(); r++) {
         for(long i = 0; i < inc.length(); i++) {
            set(r, c, get(r, c) + inc.get(r));
         }
      }
      return this;
   }

   @Override
   public long length() {
      return shape.matrixLength;
   }

   @Override
   public NDArray map(double value, @NonNull DoubleBinaryOperator operator) {
      NDArray out = zeroLike();
      for(int i = 0; i < shape.matrixLength; i++) {
         out.set(i, operator.applyAsDouble(get(i), value));
      }
      return out;
   }

   @Override
   public NDArray map(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      if(rhs.shape().isScalar()) {
         return map(rhs.scalar(), operator);
      }
      checkLength(rhs.shape());
      NDArray out = zeroLike();
      for(int i = 0; i < shape.matrixLength; i++) {
         out.set(i, operator.applyAsDouble(get(i), rhs.get(i)));
      }
      return out;
   }

   @Override
   public NDArray mapColumn(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      if(rhs.shape().isScalar()) {
         return map(rhs.scalar(), operator);
      }
      checkLength(shape.rows(), rhs.shape());
      NDArray out = zeroLike();
      for(int column = 0; column < shape.columns(); column++) {
         for(int row = 0; row < shape.rows(); row++) {
            out.set(row, column, operator.applyAsDouble(get(row, column), rhs.get(row)));
         }
      }
      return out;
   }

   @Override
   public NDArray mapColumn(int column, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      NDArray out = copy();
      return out.mapiColumn(column, rhs, operator);
   }

   @Override
   public NDArray mapRow(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      if(rhs.shape().isScalar()) {
         return map(rhs.scalar(), operator);
      }
      checkLength(shape.columns(), rhs.shape());
      NDArray out = zeroLike();
      for(int column = 0; column < shape.columns(); column++) {
         for(int row = 0; row < shape.rows(); row++) {
            out.set(row, column, operator.applyAsDouble(get(row, column), rhs.get(column)));
         }
      }
      return out;
   }

   @Override
   public NDArray mapRow(int row, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      NDArray out = copy();
      return out.mapiRow(row, rhs, operator);
   }

   @Override
   public NDArray mapi(double value, @NonNull DoubleBinaryOperator operator) {
      for(int i = 0; i < shape.matrixLength; i++) {
         set(i, operator.applyAsDouble(get(i), value));
      }
      return this;
   }

   @Override
   public NDArray mapi(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      if(rhs.shape().isScalar()) {
         return mapi(rhs.scalar(), operator);
      }
      checkLength(rhs.shape());
      for(int i = 0; i < shape.matrixLength; i++) {
         set(i, operator.applyAsDouble(get(i), rhs.get(i)));
      }
      return this;
   }

   @Override
   public NDArray mapiColumn(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      if(rhs.shape().isScalar()) {
         return mapi(rhs.scalar(), operator);
      }
      checkLength(shape.rows(), rhs.shape());
      for(int column = 0; column < shape.columns(); column++) {
         for(int row = 0; row < shape.rows(); row++) {
            set(row, column, operator.applyAsDouble(get(row, column), rhs.get(row)));
         }
      }
      return this;
   }

   @Override
   public NDArray mapiColumn(int column, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      Validation.checkArgument(column >= 0 && column < columns(), "Invalid column value" + column);
      Validation.checkArgument(rhs.length() == rows(), "Length mismatch");
      for(int r = 0; r < rows(); r++) {
         set(r, column, operator.applyAsDouble(get(r, column), rhs.get(r)));
      }
      return this;
   }

   @Override
   public NDArray mapiRow(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      if(rhs.shape().isScalar()) {
         return mapi(rhs.scalar(), operator);
      }
      checkLength(shape.columns(), rhs.shape());
      for(int column = 0; column < shape.columns(); column++) {
         for(int row = 0; row < shape.rows(); row++) {
            set(row, column, operator.applyAsDouble(get(row, column), rhs.get(column)));
         }
      }
      return this;
   }

   @Override
   public NDArray mapiRow(int row, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      Validation.checkArgument(row >= 0 && row < rows(), "Invalid row value" + row);
      Validation.checkArgument(rhs.length() == columns(), "Length mismatch");
      for(int c = 0; c < columns(); c++) {
         set(row, c, operator.applyAsDouble(get(row, c), rhs.get(c)));
      }
      return this;
   }

   @Override
   public NDArray mmul(@NonNull NDArray rhs) {
      Validation.checkArgument(rhs.shape.sliceLength == 1, () -> "Invalid Slice Length: " +
            rhs.shape.sliceLength + " != 1");
      if(shape.isVector()) {
         Validation.checkArgument(shape.matrixLength == rhs.rows());
         DenseMatrix out = new DenseMatrix(1, rhs.columns());
         for(int c = 0; c < rhs.columns(); c++) {
            for(int i = 0; i < rows(); i++) {
               out.set(c, out.get(c) + (rhs.get(i) * rhs.get(i, c)));
            }
         }
         return out;
      }
      return new DenseMatrix(toDoubleMatrix()[0].mmul(rhs.toDoubleMatrix()[0]));
   }

   @Override
   public double norm1() {
      double sum = 0;
      for(int i = 0; i < shape.matrixLength; i++) {
         sum += Math.abs(get(i));
      }
      return sum;
   }

   @Override
   public double norm2() {
      return Math.sqrt(sumOfSquares());
   }

   @Override
   public NDArray padColumnPost(int maxLength) {
      NDArray out = isDense()
                    ? new DenseMatrix(rows(), maxLength)
                    : new SparseMatrix(rows(), maxLength);
      for(int r = 0; r < rows(); r++) {
         for(int c = 0; c < Math.min(columns(), maxLength); c++) {
            out.set(r, c, get(r, c));
         }
      }
      return out;
   }

   @Override
   public NDArray padPost(int maxRowLength, int maxColumnLength) {
      NDArray out = isDense()
                    ? new DenseMatrix(maxRowLength, maxColumnLength)
                    : new SparseMatrix(maxRowLength, maxColumnLength);
      for(int r = 0; r < Math.min(rows(), maxRowLength); r++) {
         for(int c = 0; c < Math.min(columns(), maxColumnLength); c++) {
            out.set(r, c, get(r, c));
         }
      }
      return out;
   }

   @Override
   public NDArray padRowPost(int maxLength) {
      NDArray out = isDense()
                    ? new DenseMatrix(maxLength, columns())
                    : new SparseMatrix(maxLength, columns());
      for(int r = 0; r < Math.min(rows(), maxLength); r++) {
         for(int c = 0; c < columns(); c++) {
            out.set(r, c, get(r, c));
         }
      }
      return out;
   }

   @Override
   public NDArray pivot() {
      if(shape.isSquare()) {
         NDArray p = NDArrayFactory.ND.eye(shape.rows());
         for(int i = 0; i < shape.rows(); i++) {
            double max = get(i, i);
            int row = i;
            for(int j = i; j < shape.rows(); j++) {
               double v = get(j, i);
               if(v > max) {
                  max = v;
                  row = j;
               }
            }
            if(i != row) {
               NDArray v = getRow(i);
               p.setRow(i, p.getRow(row));
               p.setRow(row, v);
            }
         }
         return p;
      }
      throw new IllegalArgumentException("Only square slices supported");
   }

   @Override
   public NDArray rowArgmaxs() {
      NDArray array = new DenseMatrix(shape.rows(), 1);
      for(int r = 0; r < shape.rows(); r++) {
         double max = Double.NEGATIVE_INFINITY;
         int index = -1;
         for(int c = 0; c < shape.columns(); c++) {
            double v = get(r, c);
            if(v > max) {
               max = v;
               index = c;
            }
         }
         array.set(r, index);
      }
      return array;
   }

   @Override
   public NDArray rowArgmins() {
      NDArray array = new DenseMatrix(shape.rows(), 1);
      for(int r = 0; r < shape.rows(); r++) {
         double min = Double.POSITIVE_INFINITY;
         int index = -1;
         for(int c = 0; c < shape.columns(); c++) {
            double v = get(r, c);
            if(v < min) {
               index = c;
               min = v;
            }
         }
         array.set(r, index);
      }
      return array;
   }

   @Override
   public NDArray rowMaxs() {
      NDArray array = new DenseMatrix(shape.rows(), 1);
      for(int r = 0; r < shape.rows(); r++) {
         double max = Double.NEGATIVE_INFINITY;
         for(int c = 0; c < shape.columns(); c++) {
            max = Math.max(max, get(r, c));
         }
         array.set(r, max);
      }
      return array;
   }

   @Override
   public NDArray rowMins() {
      NDArray array = new DenseMatrix(shape.rows(), 1);
      for(int r = 0; r < shape.rows(); r++) {
         double min = Double.POSITIVE_INFINITY;
         for(int c = 0; c < shape.columns(); c++) {
            min = Math.min(min, get(r, c));
         }
         array.set(r, min);
      }
      return array;
   }

   @Override
   public NDArray rowSums() {
      NDArray array = new DenseMatrix(shape.rows(), 1);
      for(int r = 0; r < shape.rows(); r++) {
         double sum = 0;
         for(int c = 0; c < shape.columns(); c++) {
            sum += get(r, c);
         }
         array.set(r, sum);
      }
      return array;
   }

   @Override
   public NDArray set(int channel, int row, int col, double value) {
      if(channel == 0) {
         return set(row, col, value);
      }
      throw new IndexOutOfBoundsException();
   }

   @Override
   public NDArray set(int kernel, int channel, int row, int col, double value) {
      if(channel == 0 && kernel == 0) {
         return set(row, col, value);
      }
      throw new IndexOutOfBoundsException();
   }

   @Override
   public NDArray setSlice(int slice, @NonNull NDArray array) {
      Validation.checkArgument(slice == 0, "Invalid Slice: " + slice);
      checkLength(array.shape);
      for(int i = 0; i < array.shape.matrixLength; i++) {
         set(i, array.get(i));
      }
      return this;
   }

   @Override
   public NDArray slice(int slice) {
      return this;
   }

   @Override
   public NDArray sliceArgmaxs() {
      return NDArrayFactory.DENSE.scalar(argmax());
   }

   @Override
   public NDArray sliceArgmins() {
      return NDArrayFactory.DENSE.scalar(argmin());
   }

   @Override
   public NDArray sliceDot(NDArray rhs) {
      return NDArrayFactory.DENSE.scalar(dot(rhs));
   }

   @Override
   public NDArray sliceMaxs() {
      return NDArrayFactory.DENSE.scalar(max());
   }

   @Override
   public NDArray sliceMeans() {
      return NDArrayFactory.DENSE.scalar(mean());
   }

   @Override
   public NDArray sliceMins() {
      return NDArrayFactory.DENSE.scalar(min());
   }

   @Override
   public NDArray sliceNorm1() {
      return NDArrayFactory.DENSE.scalar(norm1());
   }

   @Override
   public NDArray sliceNorm2() {
      return NDArrayFactory.DENSE.scalar(norm2());
   }

   @Override
   public NDArray sliceSumOfSquares() {
      return NDArrayFactory.DENSE.scalar(sumOfSquares());
   }

   @Override
   public NDArray sliceSums() {
      return NDArrayFactory.DENSE.scalar(sum());
   }

   @Override
   public int[] sparseIndices() {
      IntArrayList list = new IntArrayList();
      for(long i = 0; i < length(); i++) {
         if(get(i) != 0) {
            list.add((int) i);
         }
      }
      return list.toArray(new int[0]);
   }

   @Override
   public double sumOfSquares() {
      double sum = 0;
      for(int i = 0; i < shape.matrixLength; i++) {
         sum += Math.pow(get(i), 2);
      }
      return sum;
   }

   @Override
   public NDArray unitize() {
      return div(norm2());
   }

   private boolean validateAllZero(int[] a) {
      a = IntStream.of(a).distinct().sorted().toArray();
      if(a.length == 0) {
         return false;
      }
      if(IntStream.of(a)
                  .anyMatch(c -> c > 0)) {
         throw new IllegalArgumentException("Illegal Slice Range: "
                                                  + Arrays.toString(a)
                                                  + shape.sliceLength);
      }
      return true;
   }

}//END OF NDArray
