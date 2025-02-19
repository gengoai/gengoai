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

package com.gengoai.apollo.math.measure;

import com.gengoai.apollo.math.linalg.NumericNDArray;

/**
 * <p>Distance measure implementation that is the negated value of a similarity measure</p>
 *
 * @author David B. Bracewell
 */
class NegativeDistanceSimilarity implements SimilarityMeasure {
   private static final long serialVersionUID = 1L;
   private final DistanceMeasure distanceMeasure;

   /**
    * Instantiates a new Negative distance similarity.
    *
    * @param distanceMeasure the distance measure
    */
   public NegativeDistanceSimilarity(DistanceMeasure distanceMeasure) {
      this.distanceMeasure = distanceMeasure;
   }

   @Override
   public DistanceMeasure asDistanceMeasure() {
      return distanceMeasure;
   }

   @Override
   public double calculate(NumericNDArray v1, NumericNDArray v2) {
      return -distanceMeasure.calculate(v1, v2);
   }

   @Override
   public double calculate(ContingencyTable table) {
      throw new UnsupportedOperationException();
   }
}//END OF NegativeDistanceSimilarity
