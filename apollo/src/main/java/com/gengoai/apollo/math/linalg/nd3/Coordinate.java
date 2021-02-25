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

import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.conversion.Cast;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A multi-dimensional coordinate in some coordinate space. Acts as the base class for <code>Index</code> and
 * <code>Shape</code>.
 */
public abstract class Coordinate implements Serializable, Comparable<Coordinate> {
   /**
    * The maximum number of dimensions supported by a Coordinate.
    */
   public static final int MAX_DIMENSIONS = 4;
   private static final long serialVersionUID = 1L;
   @JsonValue
   protected int[] point;

   /**
    * <p>Instantiates a new Index with the given dimensions.</p>
    *
    * @param axesValues axis values with the last given value starting at <code>Coordinate.MAX_DIMENSIONS - 1</code>
    */
   public Coordinate(int... axesValues) {
      this.point = new int[MAX_DIMENSIONS];
      if (axesValues != null && axesValues.length > 0) {
         int start = Math.max(0, axesValues.length - point.length);
         for (; start < axesValues.length; start++) {
            if (axesValues[start] != 0) {
               break;
            }
         }
         if (start < axesValues.length) {
            System.arraycopy(axesValues,
                             Math.max(0, axesValues.length - point.length),
                             point,
                             Math.max(0, point.length - axesValues.length),
                             Math.min(point.length, axesValues.length));
         }
      }
   }

   @Override
   public int compareTo(Coordinate o) {
      if (o == null) return 1;
      for (int i = 0; i < point.length; i++) {
         if (point[i] < o.point[i]) {
            return -1;
         } else if (point[i] > o.point[i]) {
            return 1;
         }
      }
      return 0;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (this == obj) {
         return true;
      } else if (obj instanceof Coordinate) {
         Coordinate co = Cast.as(obj);
         return Arrays.equals(point, co.point);
      }
      return false;
   }

   /**
    * <p>Gets the value at the given axis for this coordinate</p>
    *
    * @param axis the axis
    * @return the value of the given axis
    */
   public int get(int axis) {
      return point[toAbsolute(axis)];
   }

   /**
    * <p>translates an axis, which can be relative (e.g. <code>-2</code>), into its absolute axis. </p>
    *
    * @param axis the axis
    * @return the absolute axis
    * @throws IllegalArgumentException if the axis is invalid
    */
   public int toAbsolute(int axis) {
      int absAxis = axis;
      if (axis < 0) {
         absAxis = point.length + axis;
      }
      if (absAxis < 0 || absAxis >= point.length) {
         throw new IllegalArgumentException("Invalid Axis: " + axis + " for " + this);
      }
      return absAxis;
   }


}//END OF Coordinate
