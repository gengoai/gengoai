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

package com.gengoai.stream.spark;

import com.gengoai.math.EnhancedDoubleStatistics;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.local.LocalMStatisticsAccumulator;
import com.gengoai.stream.MStatisticsAccumulator;

/**
 * <p>An implementation of a {@link MStatisticsAccumulator} for Spark streams</p>
 *
 * @author David B. Bracewell
 */
public class SparkMStatisticsAccumulator extends SparkMAccumulator<Double, EnhancedDoubleStatistics> implements MStatisticsAccumulator {
   private static final long serialVersionUID = -933772215431769352L;

   /**
    * Instantiates a new SparkMStatisticsAccumulator.
    *
    * @param name the name of the accumulator
    */
   public SparkMStatisticsAccumulator(String name) {
      super(new AccumulatorV2Wrapper<>(new LocalMStatisticsAccumulator(name)));
   }

   private LocalMStatisticsAccumulator getAccumulator() {
      return Cast.as(Cast.<AccumulatorV2Wrapper>as(accumulatorV2).accumulator);
   }

   @Override
   public void add(double value) {
      getAccumulator().add(value);
   }

   @Override
   public void combine(EnhancedDoubleStatistics statistics) {
      getAccumulator().combine(statistics);
   }
}//END OF SparkMStatisticsAccumulator
