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

package com.gengoai.stream;

import com.gengoai.math.EnhancedDoubleStatistics;

/**
 * Accumulator for calculating descriptive statistics
 *
 * @author David B. Bracewell
 */
public interface MStatisticsAccumulator extends MAccumulator<Double, EnhancedDoubleStatistics> {

   /**
    * Adds a value to the statistics accumulator.
    *
    * @param value the value to add
    */
   void add(double value);

   /**
    * Combines the given statistics with this one.
    *
    * @param statistics the statistics to combine
    */
   void combine(EnhancedDoubleStatistics statistics);

}//END OF MStatisticsAccumulator
