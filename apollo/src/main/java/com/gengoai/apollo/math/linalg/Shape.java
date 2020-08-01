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
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.Copyable;
import com.gengoai.Validation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Encapsulates the dimensions, i.e. shape, of an {@link NDArray}.
 *
 * @author David B. Bracewell
 */
public class Shape implements Serializable, Copyable<Shape> {
   private static final long serialVersionUID = 1L;
   /**
    * The total length (rows * columns) of the matrix elements
    */
   public final int matrixLength;
   /**
    * The total number of slices (kernels * channels)
    */
   public final int sliceLength;
   /**
    * The Shape.
    */
   @JsonValue
   final int[] shape;

   /**
    * Creates a shape for an empty NDArray
    *
    * @return the shape
    */
   public static Shape empty() {
      return new Shape();
   }

   /**
    * Static constructor for shapes from a variable list of dimensions
    *
    * @param dims the dimensions
    * @return the shape
    */
   public static Shape shape(int... dims) {
      return new Shape(dims);
   }

   private Shape() {
      this.shape = new int[4];
      this.matrixLength = 0;
      this.sliceLength = 0;
   }

   /**
    * Instantiates a new Shape.
    *
    * @param dimensions the dimensions
    */
   @JsonCreator
   public Shape(@JsonProperty int... dimensions) {
      this.shape = new int[4];
      if(dimensions != null && dimensions.length > 0) {
         System.arraycopy(dimensions, 0, shape, shape.length - dimensions.length, dimensions.length);
         this.shape[2] = Math.max(1, this.shape[2]);
         this.shape[3] = Math.max(1, this.shape[3]);
         this.sliceLength = Math.max(1, shape[0]) * Math.max(1, shape[1]);
         this.matrixLength = shape[2] * shape[3];
      } else {
         this.shape[2] = 1;
         this.shape[3] = 1;
         this.sliceLength = 1;
         this.matrixLength = 1;
      }
      Validation.checkArgument(shape[0] >= 0, "Invalid Kernel: " + shape[0]);
      Validation.checkArgument(shape[1] >= 0, "Invalid Channel: " + shape[1]);
      Validation.checkArgument(shape[2] >= 0, "Invalid Row: " + shape[2]);
      Validation.checkArgument(shape[3] >= 0, "Invalid Column: " + shape[3]);
   }

   /**
    * Gets the number of channels
    *
    * @return the number of channels
    */
   public int channels() {
      return shape[1];
   }

   /**
    * Gets the number of columns
    *
    * @return the number of columns
    */
   public int columns() {
      return shape[3];
   }

   @Override
   public Shape copy() {
      return new Shape(this.shape);
   }

   @Override
   public boolean equals(Object obj) {
      if(this == obj) {
         return true;
      }
      if(obj == null || getClass() != obj.getClass()) {
         return false;
      }
      final Shape other = (Shape) obj;
      return Objects.deepEquals(this.shape, other.shape);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(shape);
   }

   /**
    * Checks if the shape represents a column vector
    *
    * @return True if a column vector
    */
   public boolean isColumnVector() {
      return (shape[0] == 0 && shape[1] == 0)
            && (shape[2] > 1 && shape[3] == 1);
   }

   /**
    * Checks if the shape represents a row vector
    *
    * @return True if a row vector
    */
   public boolean isRowVector() {
      return (shape[0] == 0 && shape[1] == 0)
            && (shape[2] == 1 && shape[3] > 1);
   }

   /**
    * Checks if the shape represents a scalar
    *
    * @return True if a scalar
    */
   public boolean isScalar() {
      return shape[0] == 0 && shape[1] == 0 && shape[2] == 1 && shape[3] == 1;
   }

   /**
    * Checks if the shape represents a square matrix
    *
    * @return True if a square matrix
    */
   public boolean isSquare() {
      return shape[0] == 0 && shape[1] == 0 && shape[2] == shape[3];
   }

   /**
    * Checks if the shape represents a tensor
    *
    * @return True if a tensor
    */
   public boolean isTensor() {
      return shape[0] > 0 || shape[1] > 0;
   }

   /**
    * Checks if the shape represents a vector
    *
    * @return True if a vector
    */
   public boolean isVector() {
      return (shape[0] == 0 && shape[1] == 0)
            && (shape[2] > 0 ^ shape[3] > 0);
   }

   /**
    * Gets the number of kernels
    *
    * @return the number of kernels
    */
   public int kernels() {
      return shape[0];
   }

   /**
    * Encodes the row and column into a column-major index
    *
    * @param row    the row index
    * @param column the column index
    * @return the column major index
    */
   public int matrixIndex(int row, int column) {
      return row + (shape[2] * column);
   }

   /**
    * The order of the NDArray (number of dimensions with size &gt; 1)
    *
    * @return the order
    */
   public int order() {
      int order = 0;
      for(int i1 : shape) {
         order += i1 >= 1
                  ? 1
                  : 0;
      }
      return order;
   }

   /**
    * Adjusts the shape to the new dimensions.
    *
    * @param dimensions the dimensions
    */
   public void reshape(int... dimensions) {
      Shape out = new Shape(dimensions);
      if(sliceLength != out.sliceLength) {
         throw new IllegalArgumentException("Invalid slice length: " + sliceLength + " != " + out.sliceLength);
      }
      if(matrixLength != out.matrixLength) {
         throw new IllegalArgumentException("Invalid matrix length: " + matrixLength + " != " + out.matrixLength);
      }
      System.arraycopy(out.shape, 0, shape, 0, shape.length);
   }

   /**
    * Gets the number of rows
    *
    * @return the number of rows.
    */
   public int rows() {
      return shape[2];
   }

   /**
    * Encodes the kernel and channel into a channel-major index
    *
    * @param kernel  the kernel index
    * @param channel the channel index
    * @return the channel major index
    */
   public int sliceIndex(int kernel, int channel) {
      return kernel + (shape[0] * channel);
   }

   /**
    * Decodes the channel index from  channel-major index
    *
    * @param sliceIndex the slice index
    * @return the channel index
    */
   public int toChannel(int sliceIndex) {
      return sliceIndex / shape[0];
   }

   /**
    * Decodes the column index from  column-major index
    *
    * @param matrixIndex the matrix index
    * @return the column index
    */
   public int toColumn(int matrixIndex) {
      return matrixIndex / shape[2];
   }

   /**
    * Decodes the kernel index from  channel-major index
    *
    * @param sliceIndex the slice index
    * @return the kernel index
    */
   public int toKernel(int sliceIndex) {
      return sliceIndex % shape[0];
   }

   /**
    * Decodes a slice/matrix combined index into a matrix index
    *
    * @param index the index
    * @return the matrix index
    */
   public int toMatrixIndex(long index) {
      return (int) (index / sliceLength);
   }

   /**
    * Decodes the row index from  column-major index
    *
    * @param matrixIndex the matrix index
    * @return the row index
    */
   public int toRow(int matrixIndex) {
      return matrixIndex % shape[2];
   }

   /**
    * Decodes a slice/matrix combined index into a slice index
    *
    * @param index the index
    * @return the slice index
    */
   public int toSliceIndex(long index) {
      return (int) (index % sliceLength);
   }

   @Override
   public String toString() {
      return "(" + IntStream.of(shape)
                            .filter(i -> i > 0)
                            .mapToObj(Integer::toString)
                            .collect(Collectors.joining(", ")) + ")";
   }

}//END OF Shape
