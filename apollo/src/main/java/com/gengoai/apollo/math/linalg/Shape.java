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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.Copyable;
import com.gengoai.collection.Arrays2;
import lombok.NonNull;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gengoai.Validation.checkArgument;
import static com.gengoai.apollo.math.linalg.Index.zero;

/**
 * <p>Encapsulates the dimensions, i.e. shape, of an {@link NDArray}.</p>
 *
 * @author David B. Bracewell
 */
public class Shape extends Coordinate implements Copyable<Shape> {
   /**
    * The constant KERNEL representing the axis index of a kernel.
    */
   public static final int KERNEL = MAX_DIMENSIONS - 4;
   /**
    * The constant CHANNEL representing the axis index of a channel.
    */
   public static final int CHANNEL = MAX_DIMENSIONS - 3;
   /**
    * The constant ROW representing the axis index of a row.
    */
   public static final int ROW = MAX_DIMENSIONS - 2;
   /**
    * The constant COLUMN representing the axis index of a column.
    */
   public static final int COLUMN = MAX_DIMENSIONS - 1;

   private static final long serialVersionUID = 1L;
   private int matrixLength;
   private int sliceLength;


   /**
    * <p>Instantiates a new Shape.</p>
    *
    * @param dimensions the dimensions
    */
   @JsonCreator
   protected Shape(@JsonProperty int... dimensions) {
      super(dimensions);
      validateAndCalculateLengths();
   }

   /**
    * <p>Static constructor for shapes from a variable list of dimensions</p>
    *
    * @param dimensions the dimensions
    * @return the shape
    */
   public static Shape shape(@NonNull int... dimensions) {
      return new Shape(dimensions);
   }


   /**
    * <p>Static constructor for shapes from a variable list of dimensions</p>
    *
    * @param dimensions the dimensions
    * @return the shape
    */
   public static Shape shape(@NonNull long... dimensions) {
      int[] intD = new int[dimensions.length];
      for (int i = 0; i < dimensions.length; i++) {
         intD[i] = (int) dimensions[i];
      }
      return new Shape(intD);
   }

   /**
    * <p>Determines if and how an NDArray with the given <code>rhs</code> shape cab be broadcasted onto an NDArray of
    * this shape along the given <code>axis</code>.</p>
    *
    * @param rhs the right-hand NDArray shape
    * @param axis the axis an operation will be performed on
    * @return the Broadcast type
    */
   public Broadcast accepts(@NonNull Shape rhs, int axis) {
      var absAxis = toAbsolute(axis);
      var broadcast = accepts(rhs);
      if ((broadcast == Broadcast.TENSOR_ROW || broadcast == Broadcast.TENSOR_COLUMN)
            && (absAxis == KERNEL || absAxis == CHANNEL)) {
         return Broadcast.TENSOR;
      }
      return broadcast;
   }

   /**
    * <p>Determines if and how an NDArray with the given <code>rhs</code> shape cab be broadcasted onto an NDArray of
    * this shape.</p>
    *
    * @param rhs the right-hand NDArray shape
    * @return the Broadcast type
    */
   public Broadcast accepts(@NonNull Shape rhs) {
      if (isEmpty() && rhs.isEmpty()) {
         return Broadcast.EMPTY;
      }
      if (!isEmpty() && rhs.isScalar()) {
         return Broadcast.SCALAR;
      }
      if (isScalar()) {
         return Broadcast.ERROR;
      }
      if (isVector() && rhs.isVector() && rhs.length() == length()) {
         return Broadcast.VECTOR;
      }
      if (isMatrix() && equals(rhs)) {
         return Broadcast.MATRIX;
      }
      if (isTensor() && equals(rhs)) {
         return Broadcast.TENSOR;
      }

      if ((rhs.kernels() > 1 && rhs.kernels() != kernels())
            || (rhs.channels() > 1 && rhs.channels() != channels())
      ) {
         return Broadcast.ERROR;
      }

      if (isMatrix() && rhs.rows() == rows() && rhs.columns() <= 1) {
         return Broadcast.MATRIX_COLUMN;
      }
      if (isMatrix() && rhs.columns() == columns() && rhs.rows() <= 1) {
         return Broadcast.MATRIX_ROW;
      }

      if (isTensor() && rhs.matrixShape().equals(matrixShape())) {

         if (channels() != rhs.channels()) {
            return Broadcast.TENSOR_CHANNEL;
         }

         if (kernels() != rhs.kernels()) {
            return Broadcast.TENSOR_KERNEL;
         }

         return Broadcast.TENSOR;
      }
      if (isTensor() && rhs.rows() == rows() && rhs.columns() <= 1) {
         return Broadcast.TENSOR_COLUMN;
      }
      if (isTensor() && rhs.columns() == columns() && rhs.rows() <= 1) {
         return Broadcast.TENSOR_ROW;
      }
      return Broadcast.ERROR;
   }


   /**
    * <p>Constructs an Index from this shape modifying the given axis and position pairs. Invocation is done as
    * follows: <code>asIndex(Shape.ROW, 4, Shape.COLUMN, 10)</code> will create a new Index with this shape's kernels
    * and channels and have 4 rows and 10 columns.</p>
    *
    * @param axisValuePairs the axis value pairs
    * @return the index
    */
   public Index asIndex(int... axisValuePairs) {
      if (axisValuePairs == null || axisValuePairs.length == 0) {
         return new Index(this);
      }
      checkArgument(axisValuePairs.length % 2 == 0, "Usage error: must provide an axis and its value, e.g. asIndex(axis,value,...)");
      Index ii = new Index(this);
      for (int i = 0; i < axisValuePairs.length; i += 2) {
         ii.set(axisValuePairs[i], axisValuePairs[i + 1]);
      }
      return ii;
   }

   /**
    * <p>Takes the given coordinate and modifies it so that an NDArray of this shape can be broadcasted to i.</p>
    *
    * @param coordinate the coordinate
    * @return the broadcasted index
    */
   public Index broadcast(@NonNull Coordinate coordinate) {
      Index ii = new Index(coordinate);
      for (int i = 0; i < Coordinate.MAX_DIMENSIONS; i++) {
         if (this.point[i] <= 1) {
            ii.point[i] = 0;
         }
      }
      return ii;
   }

   /**
    * <p>Creates an {@link Index} from the given offset.</p>
    *
    * @param offset the offset
    * @return the Index
    */
   public Index calculateIndex(long offset) {
      int slice = toSliceIndex(offset);
      int matrix = toMatrixIndex(offset);
      int kernel = slice / Math.max(1, point[CHANNEL]);
      int channel = slice % Math.max(1, point[CHANNEL]);
      return Index.index(kernel,
                         channel,
                         matrix % Math.max(1, point[ROW]),
                         matrix / Math.max(1, point[ROW]));
   }

   /**
    * <p>Calculates the index of the matrix associated with the given coordinate.</p>
    *
    * @param coordinate the coordinate
    * @return the matrix index
    */
   public int calculateMatrixIndex(@NonNull Coordinate coordinate) {
      return calculateMatrixIndex(coordinate.get(Shape.ROW), coordinate.get(Shape.COLUMN));
   }

   /**
    * <p>Calculates the index of the matrix associated with the given row and column.</p>
    *
    * @param row    the row
    * @param column the column
    * @return the matrix index
    */
   public int calculateMatrixIndex(int row, int column) {
      return row + (Math.max(rows(), 1) * column);
   }

   public long calculateOffset(@NonNull Coordinate c) {
      return (long) calculateSliceIndex(c) * matrixLength + calculateMatrixIndex(c);
   }

   /**
    * <p>Calculates the index of the slice associated with the given coordinate.</p>
    *
    * @param coordinate the coordinate
    * @return the slice index
    */
   public int calculateSliceIndex(@NonNull Coordinate coordinate) {
      return calculateSliceIndex(coordinate.get(Shape.KERNEL), coordinate.get(Shape.CHANNEL));
   }

   /**
    * <p>Calculates the index of the slice associated with the given kernel and channel.</p>
    *
    * @param kernel  the kernel
    * @param channel the channel
    * @return the slice index
    */
   public int calculateSliceIndex(int kernel, int channel) {
      return (kernel * Math.max(1, channels())) + channel;
   }

   /**
    * <p>The number of channels in the shape</p>
    *
    * @return the number of channels in the shape
    */
   public int channels() {
      return get(Shape.CHANNEL);
   }

   /**
    * <p>The number of columns in the shape</p>
    *
    * @return the number of columns in the shape
    */
   public int columns() {
      return get(Shape.COLUMN);
   }

   /**
    * <p>Checks if the given Coordinate is contained within this shape</p>
    *
    * @param o the coordinate to check
    * @return True if the given coordinate is contained within this shape
    */
   @NonNull
   public boolean contains(@NonNull Coordinate o) {
      for (int i = 0; i < point.length; i++) {
         if (o.point[i] > 0 && o.point[i] >= point[i]) {
            return false;
         }
      }
      return true;
   }

   @Override
   public Shape copy() {
      return new Shape(point);
   }

   public boolean isColumnVector() {
      if (point[0] > 0 || point[1] > 0) {
         return false;
      }
      return (point[2] > 0 && point[3] <= 1);
   }

   /**
    * <p>Checks if the shape has no axes</p>
    *
    * @return True if the shape is empty
    */
   public boolean isEmpty() {
      return point[0] == 0 && point[1] == 0 && point[2] == 0 && point[3] == 0;
   }

   /**
    * <p>Checks if the shape represents a matrix</p>
    *
    * @return True if a tensor
    */
   public boolean isMatrix() {
      return point[0] == 0 && point[1] == 0 && point[2] > 0 && point[3] > 0;
   }

   /**
    * <p>Checks if the shape represents a scalar</p>
    *
    * @return True if a scalar
    */
   public boolean isScalar() {
      return point[0] == 0 && point[1] == 0 && point[2] == 0 && point[3] == 1;
   }

   /**
    * <p>Checks if the shape represents a square matrix</p>
    *
    * @return True if a square matrix
    */
   public boolean isSquare() {
      return point[0] == 0 && point[1] == 0 && point[2] == point[3];
   }

   /**
    * <p>Checks if the shape represents a tensor</p>
    *
    * @return True if a tensor
    */
   public boolean isTensor() {
      return point[0] > 0 || point[1] > 0;
   }

   /**
    * <p>Checks if the shape represents a vector</p>
    *
    * @return True if a vector
    */
   public boolean isVector() {
      if (point[0] > 0 || point[1] > 0) {
         return false;
      }
      return (point[2] <= 1 && point[3] > 0) || (point[2] > 0 && point[3] <= 1);
   }

   /**
    * <p>Creates an {@link IndexRange} that iterates along the given axis at the given position.</p>
    *
    * @param axis     the axis to iterate along
    * @param position the position on the axis to iterate along
    * @return the IndexRange
    */
   public IndexRange iterateAlong(int axis, int position) {
      return Index.zero()
                  .set(axis, position)
                  .boundedIteratorTo(asIndex(axis, position + 1));
   }

   /**
    * <p>The number of kernels in the shape</p>
    *
    * @return the number of kernels in the shape
    */
   public int kernels() {
      return get(Shape.KERNEL);
   }

   /**
    * <p>The total number of elements needed to represent this Shape</p>
    *
    * @return the total number of elements needed to represent this Shape
    */
   public long length() {
      return Math.max(1L, sliceLength) * Math.max(1L, matrixLength);
   }

   /**
    * <p>The total number of elements needed to represent a matrix in this Shape</p>
    *
    * @return the total number of elements needed to represent a matrix in this Shape
    */
   public int matrixLength() {
      return matrixLength;
   }

   /**
    * <p>Creates a shape covering just the rows and columns of this Shape</p>
    *
    * @return A shape covering the rows and columns of this shape
    */
   public Shape matrixShape() {
      return Shape.shape(rows(), columns());
   }

   /**
    * <p>Creates an {@link IndexRange} that covers all coordinates covered by this shape.</p>
    *
    * @return the IndexRange
    */
   public IndexRange range() {
      return new IntervalRange(Index.zero(),
                               copy(),
                               false);
   }

   /**
    * <p>The rank of the shape, i.e. number of axes that are greater than zero.</p>
    *
    * @return the rank of the shape
    */
   public int rank() {
      return (int) Arrays.stream(point).filter(i -> i > 0).count();
   }

   /**
    * <p>Collapses the shape by removing the given axes.</p>
    *
    * @param axis  the first axis to remove
    * @param other the other axes to remove
    * @return the new shape with the axes removed
    */
   public Shape remove(int axis, @NonNull int... other) {
      return remove(Arrays2.concat(new int[]{axis}, other));
   }

   /**
    * <p>Collapses the shape by removing the given axes.</p>
    *
    * @param axes the axes to remove
    * @return the new shape with the axes removed
    */
   public Shape remove(@NonNull int[] axes) {
      if (isEmpty() || isScalar()) {
         return copy();
      }
      int[] point = copy().point;
      for (int i : axes) {
         point[toAbsolute(i)] = 0;
      }
      Shape out = shape(IntStream.of(point).filter(i -> i > 1).toArray());
      out.point[Coordinate.MAX_DIMENSIONS - 1] = Math.max(1, out.point[Coordinate.MAX_DIMENSIONS - 1]);
      return out;
   }

   /**
    * <p>Updates the axes in this shape to the new shape given that the new shape has the same total length.</p>
    *
    * @param shape the new shape to take on
    */
   public void reshape(@NonNull Shape shape) {
      checkArgument(length() == shape.length(),
                    () -> "Cannot change the length from " + length() + " to " + shape.length());
      System.arraycopy(shape.point, 0, this.point, 0, shape.point.length);
   }

   /**
    * <p>The number of rows in the shape</p>
    *
    * @return the number of rows in the shape
    */
   public int rows() {
      return get(Shape.ROW);
   }

   /**
    * <p>Creates an {@link IndexRange} to that iterates over the slices of this shape.</p>
    *
    * @return the IndexRange
    */
   public IndexRange sliceIterator() {
      return zero().iteratorTo(this, Index.index(1, 1, 0, 0));
   }

   /**
    * <p>Creates an {@link IndexRange} to that iterates over the slices of this shape along the given axis and at the
    * given position.</p>
    *
    * @param axis     the axis to iterate along (KERNEL or CHANNEL)
    * @param position the position on the axis to iterate along
    * @return the IndexRange
    */
   public IndexRange sliceIterator(int axis, int position) {
      int absAxis = toAbsolute(axis);
      if (absAxis == Shape.KERNEL || absAxis == Shape.CHANNEL) {
         return zero().set(absAxis, position)
                      .boundedIteratorTo(asIndex(absAxis, position + 1), Index.index(1, 1, 0, 0));
      }
      throw new IllegalArgumentException("Axis (" + axis + " ) is not one of KERNEL or CHANNEl");
   }

   /**
    * <p>The number of slices required to represent this shape.</p>
    *
    * @return the number of slices required to represent this shape.
    */
   public int sliceLength() {
      return sliceLength;
   }


   /**
    * <p>This shape as an array of long</p>
    *
    * @return the long array representing this shape
    */
   public long[] toLongArray() {
      return Arrays.stream(point).filter(i -> i > 0).mapToLong(i -> i).toArray();
   }

   /**
    * <p>Decodes a slice/matrix combined index into a matrix index</p>
    *
    * @param index the index
    * @return the matrix index
    */
   public int toMatrixIndex(long index) {
      int slice = toSliceIndex(index);
      return (int) index - (slice * matrixLength);
   }


   /**
    * <p>Decodes a slice/matrix combined index into a slice index</p>
    *
    * @param index the index
    * @return the slice index
    */
   public int toSliceIndex(long index) {
      return (int) index / matrixLength;
   }

   @NonNull
   public String toString() {
      return "(" + Arrays.stream(point)
                         .filter(l -> l > 0)
                         .mapToObj(Long::toString)
                         .collect(Collectors.joining(", ")) + ")";
   }


   private void validateAndCalculateLengths() {
      for (int length = point.length - 1; length >= 0; length--) {
         if (point[length] == 0 && length - 1 >= 0 && point[length - 1] > 0) {
            point[length] = 1;
         }
      }
      this.sliceLength = Math.max(1, get(Shape.KERNEL)) * Math.max(1, get(Shape.CHANNEL));
      this.matrixLength = Math.max(1, get(Shape.ROW)) * Math.max(1, get(Shape.COLUMN));
   }

   /**
    * <p>Constructs a new Shape from this one modifying the given axis and position pairs. Invocation is done as
    * follows: <code>with(Shape.ROW, 4, Shape.COLUMN, 10)</code> will create a new shape with this shape's kernels and
    * channels and have 4 rows and 10 columns.</p>
    *
    * @param axisValuePairs the axis value pairs
    * @return the shape
    */
   public Shape with(@NonNull int... axisValuePairs) {
      checkArgument(axisValuePairs.length % 2 == 0,
                    "Usage error: must provide an axis and its value, e.g. with(axis,value,...)");
      Shape copy = copy();
      for (int i = 0; i < axisValuePairs.length; i += 2) {
         copy.point[toAbsolute(axisValuePairs[i])] = axisValuePairs[i + 1];
      }
      copy.validateAndCalculateLengths();
      return copy;
   }


}//END OF Shape

