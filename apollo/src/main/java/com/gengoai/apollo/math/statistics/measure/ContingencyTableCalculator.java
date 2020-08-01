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


/**
 * <p>Calculates a value based on a contingency table. Examples include, affinity measures like mutual information.</p>
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface ContingencyTableCalculator {


   /**
    * Calculates the value of this metric.
    *
    * @param table the table to calculate on
    * @return the metric value
    */
   double calculate(ContingencyTable table);

   /**
    * If possible, calculates a p-value for the metric to determine the significance of the measure.
    *
    * @param table the table to calculate on
    * @return the p-values
    */
   default double pValue(ContingencyTable table) {
      throw new UnsupportedOperationException();
   }


}//END OF ContingencyTableCalculator
