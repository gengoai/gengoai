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
import com.gengoai.apollo.math.linalg.decompose.SingularValueDecomposition;
import lombok.NonNull;

import java.util.List;

public enum VectorCompositions implements VectorComposition {
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
   Average {
      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         return Sum.compose(vectors).divi(vectors.size());
      }
   },
   Max {
      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         Validation.checkArgument(vectors.size() > 0, "Must supply at least one vector");
         NumericNDArray toReturn = null;
         for (NumericNDArray v : vectors) {
            if (toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.mapiDouble(v, Math::max);
            }
         }
         return toReturn;
      }
   },
   Min {
      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         Validation.checkArgument(vectors.size() > 0, "Must supply at least one vector");
         NumericNDArray toReturn = null;
         for (NumericNDArray v : vectors) {
            if (toReturn == null) {
               toReturn = v.copy();
            } else {
               toReturn.mapiDouble(v, Math::min);
            }
         }
         return toReturn;
      }
   },
   PointWiseMultiply{
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
   },
   SVD {
      private final SingularValueDecomposition svd = new SingularValueDecomposition(1);

      @Override
      public NumericNDArray compose(@NonNull List<NumericNDArray> vectors) {
         Validation.checkArgument(vectors.size() > 0, "Must supply at least one vector");
         return null;// svd.decompose(nd.vstack(vectors))[2].getAxis(Shape.ROW, 0);
      }
   }
}//END OF VectorCompositions
