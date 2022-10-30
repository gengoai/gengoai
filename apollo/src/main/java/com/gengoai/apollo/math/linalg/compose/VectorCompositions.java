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

package com.gengoai.apollo.math.linalg.compose;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import lombok.NonNull;

import java.util.List;

/**
 * <p>Common Vector Compositions</p>
 *
 * @author David B. Bracewell
 */
public enum VectorCompositions implements VectorComposition {
   /**
    * Combines NDArray by summing their elements
    */
   Sum {
      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         Validation.checkArgument(vectors.size() > 0, "Must supply at least one vector");
         NumericNDArray toReturn = null;
         for (NumericNDArray v : vectors) {
            if (toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.addi(v);
            }
         }
         return toReturn;
      }
   },
   /**
    * Combines NDArray by taking the average of the elements
    */
   Average {
      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         return Sum.compose(vectors).divi(vectors.size());
      }
   },
   /**
    * Combines NDArrays taking the maximum value at each index
    */
   Max {
      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         Validation.checkArgument(vectors.size() > 0, "Must supply at least one vector");
         NumericNDArray toReturn = null;
         for (NumericNDArray v : vectors) {
            if (toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.mapi(v, Math::max);
            }
         }
         return toReturn;
      }
   },
   /**
    * Combines NDArrays taking the minimum value at each index
    */
   Min {
      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         Validation.checkArgument(vectors.size() > 0, "Must supply at least one vector");
         NumericNDArray toReturn = null;
         for (NumericNDArray v : vectors) {
            if (toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.mapi(v, Math::min);
            }
         }
         return toReturn;
      }
   },
   /**
    * Combines NDArray by multiplying the elements in a point-wise fashion
    */
   PointWiseMultiply {
      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         Validation.checkArgument(vectors.size() > 0, "Must supply at least one vector");
         NumericNDArray toReturn = null;
         for (NumericNDArray v : vectors) {
            if (toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.muli(v);
            }
         }
         return toReturn;
      }
   }
}//END OF VectorCompositions
