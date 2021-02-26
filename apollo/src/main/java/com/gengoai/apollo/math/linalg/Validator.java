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


public final class Validator {
   public static final String THIS_NUMERIC = "Operation cannot be performed on NDArray of type '%s'.";
   public static final String ARG_NUMERIC = "Operation cannot be performed with an NDArray argument of type '%s'.";
   public static final String LENGTH_MISMATCH = "Length mismatch %d != %d.";
   public static final String LENGTH_MISMATCH_AXIS = "Length mismatch %d != %d for axis %d (%d) of shape %s.";
   public static final String INVALID_AXIS = "Invalid axis %s for shape %s.";
   public static final String INVALID_DIMENSION = "Invalid dimension %d for axis %d (%d) of shape %s.";
   public static final String INVALID_DIMENSION_RANGE = "Invalid dimension range from=%d to=%d for axis %d (%d) of shape %s.";

   public static void checkArgsAreNumeric(NDArray<?> thisNDArray, NDArray<?> otherNDArray) {
      checkThisIsNumeric(thisNDArray);
      if (!otherNDArray.isNumeric()) {
         throw new IllegalArgumentException(String.format(ARG_NUMERIC, otherNDArray.getType().getSimpleName()));
      }
   }

   public static int checkAxis(int axis, NDArray<?> n) {
      int absAxis = n.shape().toAbsolute(axis);
      if (absAxis < (Shape.MAX_DIMENSIONS - n.rank()) || absAxis >= Shape.MAX_DIMENSIONS) {
         throw new IllegalArgumentException(String.format(INVALID_AXIS, absAxis, n.shape()));
      }
      return absAxis;
   }

   public static void checkDimension(int axis, int dimension, NDArray<?> n) {
      if (dimension < 0 || ( dimension > 0 && dimension >= n.shape().get(axis)) ) {
         throw new IllegalArgumentException(String.format(INVALID_DIMENSION, dimension, axis, n.shape()
                                                                                               .toAbsolute(axis), n
                                                                .shape()));
      }
   }

   public static void checkLengthMatch(NDArray<?> lhs, NDArray<?> rhs) {
      if (lhs.length() != rhs.length())
         throw new IllegalArgumentException(String.format(LENGTH_MISMATCH, rhs.length(), lhs.length()));
   }


   public static void checkThisIsNumeric(NDArray<?> a) {
      if (!a.isNumeric()) {
         throw new IllegalArgumentException(String.format(THIS_NUMERIC, a.getType().getSimpleName()));
      }
   }


}
