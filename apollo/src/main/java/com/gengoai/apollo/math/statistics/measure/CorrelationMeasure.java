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

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.util.FastMath;

/**
 * <p>Defines methodology to determine how related two items are.</p>
 *
 * @author David B. Bracewell
 */
public interface CorrelationMeasure extends SimilarityMeasure {

   @Override
   default double calculate(NDArray v1, NDArray v2) {
      Validation.checkArgument(v1.shape().isVector() && v2.shape().isVector(), "v1 and v2 must be bectors");
      return calculate(v1.toDoubleArray(), v2.toDoubleArray());
   }

   @Override
   default double calculate(ContingencyTable table) {
      throw new UnsupportedOperationException();
   }

   @Override
   double calculate(double[] v1, double[] v2);


   /**
    * Calculates the p-value for the correlation coefficient when N &gt;= 6 using a one-tailed t-Test.
    *
    * @param r the correlation coefficient.
    * @param N the number of items
    * @return the non-directional p-value
    */
   default double pValue(double r, int N) {
      Validation.checkArgument(N >= 6, "N must be >= 6.");
      double t = (r * FastMath.sqrt(N - 2.0)) / FastMath.sqrt(1.0 - r * r);
      return 1.0 - new TDistribution(N - 2, 1)
                      .cumulativeProbability(t);
   }


}//END OF CorrelationMeasure
