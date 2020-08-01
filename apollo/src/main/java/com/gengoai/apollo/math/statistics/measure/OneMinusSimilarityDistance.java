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

package com.gengoai.apollo.math.statistics.measure;

import com.gengoai.apollo.math.linalg.NDArray;

/**
 * <p>Distance measure implementation that is the one minus the value of a similarity measure</p>
 *
 * @author David B. Bracewell
 */
class OneMinusSimilarityDistance implements DistanceMeasure {
   private static final long serialVersionUID = 1L;
   private final SimilarityMeasure similarityMeasure;

   /**
    * Instantiates a new One minus similarity distance.
    *
    * @param similarityMeasure the similarity measure
    */
   public OneMinusSimilarityDistance(SimilarityMeasure similarityMeasure) {
      this.similarityMeasure = similarityMeasure;
   }

   @Override
   public SimilarityMeasure asSimilarityMeasure() {
      return similarityMeasure;
   }

   @Override
   public double calculate(NDArray v1, NDArray v2) {
      return 1d - similarityMeasure.calculate(v1,v2);
   }

}//END OF OneMinusSimilarityDistance
