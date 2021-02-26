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

package com.gengoai.apollo.math.linalg.composition;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.linalg.nd;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;

public class Sum<T extends Number> implements VectorComposition<T, T> {
   private static final long serialVersionUID = 1L;


   @Override
   public NDArray<T> compose(@NonNull List<NDArray<T>> vectors) {
      Validation.checkArgument(vectors.size() > 0, "Must supply at least one vector");
      NDArray<T> toReturn = null;
      for (NDArray<T> v : vectors) {
         if (toReturn == null) {
            toReturn = v.copy();
         } else {
            toReturn.addi(v);
         }
      }
      return toReturn;
   }
}//END OF Sum
