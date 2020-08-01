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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Validation;
import lombok.NonNull;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Three and Four dimensional NDArrays.
 *
 * @author David B. Bracewell
 */
public class Tensor extends NDArray {
   private static final long serialVersionUID = 1L;
   @JsonProperty("slices")
   final NDArray[] slices;

   /**
    * Instantiates a new Tensor.
    *
    * @param kernels  the kernels
    * @param channels the channels
    * @param slices   the slices
    */
   public Tensor(int kernels, int channels, @NonNull NDArray[] slices) {
      super(Shape.shape(kernels, channels, slices[0].rows(), slices[0].columns()));
      this.slices = slices;
   }

   /**
    * Instantiates a new Tensor.
    *
    * @param shape the shape
    */
   public Tensor(@NonNull Shape shape) {
      super(shape);
      this.slices = new NDArray[shape.sliceLength];
   }

   public Tensor(@NonNull List<NDArray> slices) {
      super(Shape.shape(0, slices.size(), slices.get(0).rows(), slices.get(0).columns()));
      this.slices = slices.toArray(NDArray[]::new);
   }

   @JsonCreator
   protected Tensor(@JsonProperty("slices") NDArray[] slices,
                    @JsonProperty("shape") Shape shape,
                    @JsonProperty("label") Object label,
                    @JsonProperty("predicted") Object predicted,
                    @JsonProperty("weight") double weight) {
      this(shape.kernels(), shape.channels(), slices);
      setLabel(label);
      setPredicted(predicted);
      setWeight(weight);
   }

   @Override
   public NDArray T() {
      return mapSlices(NDArray::T);
   }

   @Override
   public long argmax() {
      long index = 0;
      double max = Double.NEGATIVE_INFINITY;
      for(int i = 0; i < slices.length; i++) {
         long im = slices[i].argmax();
         double v = slices[i].get(im);
         if(v > max) {
            index = im * i;
            max = v;
         }
      }
      return index;
   }

   @Override
   public long argmin() {
      long index = 0;
      double min = Double.POSITIVE_INFINITY;
      for(int i = 0; i < slices.length; i++) {
         long im = slices[i].argmin();
         double v = slices[i].get(im);
         if(v < min) {
            index = im * i;
            min = v;
         }
      }
      return index;
   }

   private void check(Shape shape) {
      if(shape.sliceLength > 1 && shape.sliceLength != shape().sliceLength) {
         throw new IllegalArgumentException(
               "Invalid Slice Length: " + shape.sliceLength + " != " + shape().sliceLength);
      }
      if(shape.matrixLength != shape().matrixLength) {
         throw new IllegalArgumentException(
               "Invalid Matrix Length: " + shape.matrixLength + " != " + shape().matrixLength);
      }
   }

   private void check(int target, Shape shape) {
      if(shape.sliceLength > 1 && shape.sliceLength != shape().sliceLength) {
         throw new IllegalArgumentException(
               "Invalid Slice Length: " + shape.sliceLength + " != " + shape().sliceLength);
      }
      if(shape.matrixLength != target) {
         throw new IllegalArgumentException(
               "Invalid Matrix Length: " + shape.matrixLength + " != " + target);
      }
   }

   @Override
   public NDArray columnArgmaxs() {
      return mapSlices(NDArray::columnArgmaxs);
   }

   @Override
   public NDArray columnArgmins() {
      return mapSlices(NDArray::columnArgmins);
   }

   @Override
   public NDArray columnMaxs() {
      return mapSlices(NDArray::columnMaxs);
   }

   @Override
   public NDArray columnMins() {
      return mapSlices(NDArray::columnMins);
   }

   @Override
   public NDArray columnSums() {
      return mapSlices(NDArray::columnSums);
   }

   @Override
   public NDArray compact() {
      for(NDArray slice : slices) {
         slice.compact();
      }
      return this;
   }

   @Override
   public NDArray diag() {
      return mapSlices(NDArray::diag);
   }

   @Override
   public double dot(@NonNull NDArray rhs) {
      check(columns(), rhs.shape);
      double dot = 0;
      for(int i = 0; i < slices.length; i++) {
         dot += slices[i].dot(rhs.slice(i));
      }
      return dot;
   }

   @Override
   public NDArray fill(double value) {
      return mapiSlices(v -> v.fill(value));
   }

   @Override
   public void forEachSparse(@NonNull EntryConsumer consumer) {
      for(int i = 0; i < slices.length; i++) {
         int slice = i;
         slices[i].forEachSparse((mi, v) -> consumer.apply(mi * slice, v));
      }
   }

   @Override
   public double get(long i) {
      return slices[shape.toSliceIndex(i)].get(shape.toMatrixIndex(i));
   }

   @Override
   public double get(int row, int col) {
      return slices[0].get(row, col);
   }

   @Override
   public double get(int channel, int row, int col) {
      return slices[shape.sliceIndex(0, channel)].get(row, col);
   }

   @Override
   public double get(int kernel, int channel, int row, int col) {
      return slices[shape.sliceIndex(kernel, channel)].get(row, col);
   }

   @Override
   public NDArray getColumn(int column) {
      return mapSlices(n -> n.getRow(column));
   }

   @Override
   public NDArray getColumns(int[] columns) {
      return mapSlices(n -> n.getColumns(columns));
   }

   @Override
   public NDArray getColumns(int from, int to) {
      return mapSlices(n -> n.getColumns(from, to));
   }

   @Override
   public NDArray getRow(int row) {
      return mapSlices(n -> n.getRow(row));
   }

   @Override
   public NDArray getRows(int[] rows) {
      return mapSlices(n -> n.getRows(rows));
   }

   @Override
   public NDArray getRows(int from, int to) {
      return mapSlices(n -> n.getRows(from, to));
   }

   @Override
   public NDArray getSubMatrix(int fromRow, int toRow, int fromCol, int toCol) {
      return mapSlices(n -> n.getSubMatrix(fromRow, toRow, fromCol, toCol));
   }

   @Override
   public NDArray incrementiColumn(int c, NDArray vector) {
      return mapiSlices(n -> n.incrementiColumn(c, vector));
   }

   @Override
   public boolean isDense() {
      return slices[0].isDense();
   }

   @Override
   public NDArray map(@NonNull DoubleUnaryOperator operator) {
      NDArray[] out = new NDArray[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = slices[i].map(operator);
      }
      return new Tensor(kernels(), channels(), out);
   }

   @Override
   public NDArray map(double value, @NonNull DoubleBinaryOperator operator) {
      NDArray[] out = new NDArray[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = slices[i].map(value, operator);
      }
      return new Tensor(kernels(), channels(), out);
   }

   @Override
   public NDArray map(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      check(rhs.shape);
      NDArray[] out = new NDArray[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = slices[i].map(rhs.slice(i), operator);
      }
      return new Tensor(kernels(), channels(), out);
   }

   @Override
   public NDArray mapColumn(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      check(rows(), rhs.shape);
      NDArray[] out = new NDArray[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = slices[i].mapColumn(rhs.slice(i), operator);
      }
      return new Tensor(kernels(), channels(), out);
   }

   @Override
   public NDArray mapColumn(int column, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      return mapSlices(s -> s.mapColumn(column, rhs, operator));
   }

   @Override
   public NDArray mapRow(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      check(columns(), rhs.shape);
      NDArray[] out = new NDArray[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = slices[i].mapRow(rhs.slice(i), operator);
      }
      return new Tensor(kernels(), channels(), out);
   }

   @Override
   public NDArray mapRow(int row, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      return mapSlices(s -> s.mapRow(row, rhs, operator));
   }

   private NDArray mapSlices(Function<NDArray, NDArray> operator) {
      NDArray[] out = new NDArray[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = operator.apply(slices[i]);
      }
      return new Tensor(kernels(), channels(), out);
   }

   private NDArray mapSlices(NDArray o, BiFunction<NDArray, NDArray, NDArray> operator) {
      check(o.shape);
      NDArray[] out = new NDArray[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = operator.apply(slices[i], o.slice(i));
      }
      return new Tensor(kernels(), channels(), out);
   }

   @Override
   public NDArray mapi(@NonNull DoubleUnaryOperator operator) {
      for(NDArray slice : slices) {
         slice.mapi(operator);
      }
      return this;
   }

   @Override
   public NDArray mapi(double value, @NonNull DoubleBinaryOperator operator) {
      for(NDArray slice : slices) {
         slice.mapi(value, operator);
      }
      return this;
   }

   @Override
   public NDArray mapi(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      check(rhs.shape);
      for(int i = 0; i < slices.length; i++) {
         slices[i].mapi(rhs.slice(i), operator);
      }
      return this;
   }

   @Override
   public NDArray mapiColumn(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      check(rows(), rhs.shape);
      for(int i = 0; i < slices.length; i++) {
         slices[i].mapiColumn(rhs.slice(i), operator);
      }
      return this;
   }

   @Override
   public NDArray mapiColumn(int column, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      return mapiSlices(s -> s.mapiColumn(column, rhs, operator));
   }

   @Override
   public NDArray mapiRow(@NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      check(columns(), rhs.shape);
      for(int i = 0; i < slices.length; i++) {
         slices[i].mapiRow(rhs.slice(i), operator);
      }
      return this;
   }

   @Override
   public NDArray mapiRow(int row, @NonNull NDArray rhs, @NonNull DoubleBinaryOperator operator) {
      return mapiSlices(s -> s.mapiRow(row, rhs, operator));
   }

   private NDArray mapiSlices(Function<NDArray, NDArray> operator) {
      for(NDArray slice : slices) {
         operator.apply(slice);
      }
      return this;
   }

   @Override
   public double max() {
      return Stream.of(slices).mapToDouble(NDArray::max).max().orElse(Double.NEGATIVE_INFINITY);
   }

   @Override
   public double min() {
      return Stream.of(slices).mapToDouble(NDArray::min).min().orElse(Double.POSITIVE_INFINITY);
   }

   @Override
   public NDArray mmul(@NonNull NDArray rhs) {
      return mapSlices(rhs, NDArray::mmul);
   }

   @Override
   public double norm1() {
      return Stream.of(slices).mapToDouble(NDArray::norm1).sum();
   }

   @Override
   public double norm2() {
      return Math.sqrt(Stream.of(slices).mapToDouble(NDArray::sumOfSquares).sum());
   }

   @Override
   public NDArray padColumnPost(int maxLength) {
      return mapSlices(n -> n.padColumnPost(maxLength));
   }

   @Override
   public NDArray padPost(int maxRowLength, int maxColumnLength) {
      return mapSlices(n -> n.padPost(maxRowLength, maxColumnLength));
   }

   @Override
   public NDArray padRowPost(int maxLength) {
      return mapSlices(n -> n.padRowPost(maxLength));
   }

   @Override
   public NDArray pivot() {
      return mapSlices(NDArray::pivot);
   }

   @Override
   public NDArray reshape(int... dims) {
      return null;
   }

   @Override
   public NDArray rowArgmaxs() {
      return mapSlices(NDArray::rowArgmaxs);
   }

   @Override
   public NDArray rowArgmins() {
      return mapSlices(NDArray::rowArgmins);
   }

   @Override
   public NDArray rowMaxs() {
      return mapSlices(NDArray::rowMaxs);
   }

   @Override
   public NDArray rowMins() {
      return mapSlices(NDArray::rowMins);
   }

   @Override
   public NDArray rowSums() {
      return mapSlices(NDArray::rowSums);
   }

   @Override
   public NDArray set(long i, double value) {
      return slices[shape.toSliceIndex(i)].set(shape.toMatrixIndex(i), value);
   }

   @Override
   public NDArray set(int row, int col, double value) {
      return slices[0].set(row, col, value);
   }

   @Override
   public NDArray set(int channel, int row, int col, double value) {
      return slices[shape.sliceIndex(0, channel)].set(row, col, value);
   }

   @Override
   public NDArray set(int kernel, int channel, int row, int col, double value) {
      return slices[shape.sliceIndex(kernel, channel)].set(row, col, value);
   }

   @Override
   public NDArray setColumn(int i, @NonNull NDArray array) {
      check(rows(), array.shape);
      for(int j = 0; j < slices.length; j++) {
         slices[j].setColumn(i, array.slice(j));
      }
      return this;
   }

   @Override
   public NDArray setRow(int i, @NonNull NDArray array) {
      check(columns(), array.shape);
      for(int j = 0; j < slices.length; j++) {
         slices[j].setRow(i, array.slice(j));
      }
      return this;
   }

   @Override
   public NDArray setSlice(int slice, @NonNull NDArray array) {
      Validation.checkArgument(array.shape.sliceLength == 1,
                               "Invalid Slice Length: " + array.shape.sliceLength + " > 1");
      check(array.shape);
      return slices[slice] = array;
   }

   @Override
   public NDArray slice(int slice) {
      return slices[slice];
   }

   @Override
   public NDArray sliceArgmaxs() {
      return mapSlices(NDArray::sliceArgmaxs);
   }

   @Override
   public NDArray sliceArgmins() {
      return mapSlices(NDArray::sliceArgmins);
   }

   @Override
   public NDArray sliceDot(NDArray rhs) {
      return mapSlices(rhs, NDArray::sliceDot);
   }

   @Override
   public NDArray sliceMaxs() {
      return mapSlices(NDArray::sliceMaxs);
   }

   @Override
   public NDArray sliceMeans() {
      return mapSlices(NDArray::sliceMeans);
   }

   @Override
   public NDArray sliceMins() {
      return mapSlices(NDArray::sliceMeans);
   }

   @Override
   public NDArray sliceNorm1() {
      return mapSlices(NDArray::sliceNorm1);
   }

   @Override
   public NDArray sliceNorm2() {
      return mapSlices(NDArray::sliceNorm2);
   }

   @Override
   public NDArray sliceSumOfSquares() {
      return mapSlices(NDArray::sliceSumOfSquares);
   }

   @Override
   public NDArray sliceSums() {
      return mapSlices(NDArray::sliceSums);
   }

   @Override
   public int[] sparseIndices() {
      throw new UnsupportedOperationException();
   }

   @Override
   public double sum() {
      return Stream.of(slices).mapToDouble(NDArray::sum).sum();
   }

   @Override
   public double sumOfSquares() {
      return Stream.of(slices).mapToDouble(NDArray::sumOfSquares).sum();
   }

   @Override
   public double[] toDoubleArray() {
      throw new UnsupportedOperationException();
   }

   @Override
   public DoubleMatrix[] toDoubleMatrix() {
      DoubleMatrix[] out = new DoubleMatrix[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = slices[i].toDoubleMatrix()[0];
      }
      return out;
   }

   @Override
   public float[] toFloatArray() {
      if(slices.length == 1) {
         return slices[0].toFloatArray();
      }
      throw new UnsupportedOperationException();
   }

   @Override
   public float[][] toFloatArray2() {
      if(slices.length == 1) {
         return slices[0].toFloatArray2();
      }
      throw new UnsupportedOperationException();
   }

   @Override
   public float[][][] toFloatArray3() {
      if(shape.channels() > 1 && shape.kernels() > 1) {
         throw new UnsupportedOperationException();
      }
      float[][][] r = new float[slices.length][shape.rows()][shape.columns()];
      for(int i = 0; i < slices.length; i++) {
         r[i] = slices[i].toFloatArray2();
      }
      return r;
   }

   @Override
   public FloatMatrix[] toFloatMatrix() {
      FloatMatrix[] out = new FloatMatrix[shape.sliceLength];
      for(int i = 0; i < slices.length; i++) {
         out[i] = slices[i].toFloatMatrix()[0];
      }
      return out;
   }

   @Override
   public String toString() {
      return toString(10, 10, 10);
   }

   @Override
   public NDArray unitize() {
      return mapiSlices(NDArray::unitize);
   }

   @Override
   public NDArray zeroLike() {
      return isDense()
             ? NDArrayFactory.DENSE.array(shape)
             : NDArrayFactory.SPARSE.array(shape);
   }

}//END OF Tensor
