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
import com.gengoai.math.Optimum;

import java.io.Serializable;

import static com.gengoai.apollo.math.linalg.NDArrayFactory.ND;

/**
 * <p>Calculates a metric between items, such as distance and similarity.</p>
 *
 * @author David B. Bracewell
 */
public interface Measure extends Serializable {

   /**
    * Calculate this measure using two double arrays as the input
    *
    * @param v1 the first double array
    * @param v2 the second double array
    * @return the metric result
    */
   default double calculate(double[] v1, double[] v2) {
      return calculate(ND.columnVector(v1), ND.columnVector(v2));
   }

   /**
    * Calculate this measure using two vectors as the input
    *
    * @param v1 the first vector
    * @param v2 the second vector
    * @return the metric result
    */
   double calculate(NDArray v1, NDArray v2);


   /**
    * Gets what kind of optimum should be used with this measure, i.e. is bigger or smaller better.
    *
    * @return the optimum
    */
   Optimum getOptimum();

}//END OF Measure
