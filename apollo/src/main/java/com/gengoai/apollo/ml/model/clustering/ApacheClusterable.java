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

package com.gengoai.apollo.ml.model.clustering;

import com.gengoai.Lazy;
import com.gengoai.apollo.math.linalg.NDArray;
import org.apache.commons.math3.ml.clustering.Clusterable;

import java.io.Serializable;

/**
 * Wraps an Apollo NDArray into an Apache Math compatible data type for using Apache Math clustering algorithms.
 *
 * @author David B. Bracewell
 */
class ApacheClusterable implements Clusterable, Serializable {
   private static final long serialVersionUID = 1L;
   private final NDArray vector;
   private final Lazy<double[]> point;

   /**
    * Instantiates a new Apache clusterable.
    *
    * @param vector the vector to wrap
    */
   public ApacheClusterable(NDArray vector) {
      this.vector = vector;
      this.point = new Lazy<>(vector::toDoubleArray);
   }

   @Override
   public double[] getPoint() {
      return point.get();
   }

   /**
    * Gets the wrapped vector.
    *
    * @return the vector
    */
   public NDArray getVector() {
      return vector;
   }

   @Override
   public String toString() {
      return vector.toString();
   }

}// END OF ApacheClusterable
