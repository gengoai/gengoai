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

import org.tensorflow.Tensor;
import org.tensorflow.types.TFloat64;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public abstract class DoubleNDArray extends NumericNDArray {
   protected DoubleNDArray(Shape shape) {
      super(shape);
   }

   @Override
   public Tensor toTensor() {
      TFloat64 float64 = TFloat64.tensorOf(org.tensorflow.ndarray.Shape.of(shape().toLongArray()));
      float64.scalars().forEachIndexed((c, v) -> {
         v.setDouble(getDouble(Index.index(c)));
      });
      return float64;
   }

}//END OF DoubleNDArray
