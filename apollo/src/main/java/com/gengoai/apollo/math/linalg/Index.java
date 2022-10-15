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

import com.gengoai.Copyable;
import com.gengoai.Validation;
import com.gengoai.collection.Arrays2;
import lombok.NonNull;
import org.apache.mahout.math.list.IntArrayList;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * <p>A coordinate that indicates a particular position in a coordinate space.</p>
 *
 * @author David B. Bracewell
 */
public class Index extends Coordinate implements Copyable<Index> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Index with the given dimensions.
    *
    * @param axesValues axis values with the last given value starting at <code>Coordinate.MAX_DIMENSIONS - 1</code>
    */
   protected Index(int... axesValues) {
      super(axesValues);
   }




   /**
    * Copy Constructor
    *
    * @param copy the copy
    */
   protected Index(@NonNull Coordinate copy) {
      this.point = new int[MAX_DIMENSIONS];
      System.arraycopy(copy.point, 0, this.point, 0, this.point.length);
   }

   /**
    * Static Index constructor
    *
    * @param axesValues axis values with the last given value starting at <code>Coordinate.MAX_DIMENSIONS - 1</code>
    * @return the Index
    */
   public static Index index(int... axesValues) {
      return new Index(axesValues);
   }

   /**
    * Static Index constructor
    *
    * @param axesValues axis values with the last given value starting at <code>Coordinate.MAX_DIMENSIONS - 1</code>
    * @return the Index
    */
   public static Index index(long... axesValues) {
      int[] intD = new int[axesValues.length];
      for (int i = 0; i < axesValues.length; i++) {
         intD[i] = (int) axesValues[i];
      }
      return new Index(intD);
   }

   /**
    * Static constructor that creates a zero-valued index
    *
    * @return the Index
    */
   public static Index zero() {
      return new Index();
   }


   /**
    * <p>Generates an IndexRange from this Index to the given <code>stoppingPoint</code> where at each step the index
    * is incremented by the given <code>increment</code> and each increment will have a lower bounds of this index.</p>
    * <pre>{code
    *    //Given the following Index
    *    Index start = Index.index(1,2);
    *
    *    //The following ending Index
    *    Index end = Index.index(4,5);
    *
    *    //And the following increment
    *    Index increment = Index.increment(1,2)
    *
    *    //We can create the following IndexRange
    *    IndexRange range = start.boundedIteratorTo(end, increment)
    *
    *    //That will produce the following indicies
    *    // (1,2), (1,4), (2,2), (2, 4), (3,2), (3,4)
    *    // Note that the column value always resets to 2 as that is the lower bound
    * }</pre>
    *
    * @param stoppingPoint the stopping point
    * @param increment     the increment
    * @return the index range
    */
   public IndexRange boundedIteratorTo(@NonNull Coordinate stoppingPoint,
                                       @NonNull Coordinate increment) {
      return new IntervalRangeWithIncrement(this.copy(), stoppingPoint, true, increment);
   }

   /**
    * <p>Generates an IndexRange from this Index to the given <code>stoppingPoint</code> where at each step the index
    * is incremented by <code>1</code> to the next coordinate and each increment will have a lower bounds of this
    * index.</p>
    * <pre>{code
    *    //Given the following Index
    *    Index start = Index.index(1,2);
    *
    *    //The following ending Index
    *    Index end = Index.index(4,5);
    *
    *    //We can create the following IndexRange
    *    IndexRange range = start.boundedIteratorTo(end, increment)
    *
    *    //That will produce the following indicies
    *    // (1,2), (1,3), (1,4), (2,2), (2,3), (2, 4), (3,2), (3,3), (3,4)
    *    // Note that the column value always resets to 2 as that is the lower bound
    * }</pre>
    *
    * @param stoppingPoint the stopping point
    * @return the index range
    */
   public IndexRange boundedIteratorTo(@NonNull Coordinate stoppingPoint) {
      return new IntervalRange(this.copy(), stoppingPoint, true);
   }

   @Override
   public Index copy() {
      return new Index(this);
   }

   /**
    * <p>Gets the value of the channel axis.</p>
    *
    * @return the value of the channel axis
    */
   public int getChannel() {
      return get(Shape.CHANNEL);
   }

   /**
    * <p>Gets the value of the column axis.</p>
    *
    * @return the value of the column axis
    */
   public int getColumn() {
      return get(Shape.COLUMN);
   }

   /**
    * <p>Gets the value of the kernel axis.</p>
    *
    * @return the value of the kernel axis
    */
   public int getKernel() {
      return get(Shape.KERNEL);
   }

   /**
    * <p>Gets the value of the row axis.</p>
    *
    * @return the value of the row axis
    */
   public int getRow() {
      return get(Shape.ROW);
   }

   /**
    * <p>Generates an IndexRange from this Index to the given <code>stoppingPoint</code> where at each step the index
    * is incremented by the given <code>increment</code>to the next coordinate.</p>
    * <pre>{code
    *    //Given the following Index
    *    Index start = Index.index(1,2);
    *
    *    //The following ending Index
    *    Index end = Index.index(4,5);
    *
    *    //And the following increment
    *    Index increment = Index.increment(1,2)
    *
    *    //We can create the following IndexRange
    *    IndexRange range = start.boundedIteratorTo(end, increment)
    *
    *    //That will produce the following indicies
    *    // (1,2), (1,4),
    *    // (2,0), (2,2), (2, 4),
    *    // (3,0), (3,2), (3,4)
    * }</pre>
    *
    * @param stoppingPoint the stopping point
    * @param increment     the increment
    * @return the index range
    */
   public IndexRange iteratorTo(@NonNull Coordinate stoppingPoint,
                                @NonNull Coordinate increment) {
      return new IntervalRangeWithIncrement(this.copy(), stoppingPoint, false, increment);
   }

   /**
    * <p>Generates an IndexRange from this Index to the given <code>stoppingPoint</code> where at each step the index
    * is incremented by <code>1</code> to the next coordinate.</p>
    * <pre>{code
    *    //Given the following Index
    *    Index start = Index.index(1,2);
    *
    *    //The following ending Index
    *    Index end = Index.index(4,5);
    *
    *    //We can create the following IndexRange
    *    IndexRange range = start.boundedIteratorTo(end, increment)
    *
    *    //That will produce the following indicies
    *    // (1,2), (1,3), (1,4),
    *    // (2,0), (2,1), (2,2), (2,3), (2, 4),
    *    // (3,0), (3,1), (3,2), (3,3), (3,4)
    * }</pre>
    *
    * @param stoppingPoint the stopping point
    * @return the index range
    */
   public IndexRange iteratorTo(@NonNull Coordinate stoppingPoint) {
      return new IntervalRange(this.copy(), stoppingPoint, false);
   }

   /**
    * <p>Sets the value of the given axis</p>
    *
    * @param axis  the axis
    * @param value the value
    * @return this Index
    */
   public Index set(int axis, int value) {
      Validation.checkArgument(value >= 0, () -> "Invalid value " + value + " for axis " + axis);
      this.point[toAbsolute(axis)] = value;
      return this;
   }


   /**
    * <p>Subtracts the given coordinate from this Index returning a new Index with the result.</p>
    *
    * @param c the coordinate to subtract
    * @return the new Index which is the result of the subtraction
    */
   public Index subtract(@NonNull Coordinate c) {
      Index out = new Index();
      for (int i = 0; i < point.length; i++) {
         out.point[i] = point[i] - c.point[i];
      }
      return out;
   }

   @NonNull
   public String toString() {
      int start = 0;
      for (; start < point.length - 1; start++) {
         if (point[start] != 0) {
            break;
         }
      }
      return "(" + Arrays.stream(point, start, point.length)
                         .mapToObj(Long::toString)
                         .collect(Collectors.joining(", ")) + ")";
   }

   /**
    * <p>Collapses the index by removing the given axes.</p>
    *
    * @param axis  the first axis to remove
    * @param other the other axes to remove
    * @return the new index with the axes removed
    */
   public Index remove(int axis, @NonNull int... other) {
     return remove(Arrays2.concat(new int[]{axis}, other));
   }

   /**
    * <p>Collapses the index by removing the given axes.</p>
    *
    * @param axes the axes to remove
    * @return the new index with the axes removed
    */
   public Index remove(@NonNull int[] axes) {
      int[] point = copy().point;
      for (int i : axes) {
         point[toAbsolute(i)] = -1;
      }
      IntArrayList al = new IntArrayList();
      boolean mustAdd = false;
      for (int j : point) {
         if (j != -1 && (j > 0 || mustAdd)) {
            mustAdd = true;
            al.add(j);
         }
      }
      int[] out = new int[al.size()];
      return Index.index(al.toArray(out));
   }


}//END OF Index
