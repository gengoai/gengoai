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

import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.MMultiCounterAccumulator;
import com.gengoai.stream.local.LocalMMultiCounterAccumulator;
import com.gengoai.tuple.Tuple2;

/**
 * <p>An implementation of a {@link MMultiCounterAccumulator} for Spark streams</p>
 *
 * @param <K1> the first key type parameter of the MultiCounter
 * @param <K2> the second key type parameter of the MultiCounter
 * @author David B. Bracewell
 */
public class SparkMMultiCounterAccumulator<K1, K2> extends SparkMAccumulator<Tuple2<K1, K2>, MultiCounter<K1, K2>> implements MMultiCounterAccumulator<K1, K2> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Spark SparkMMultiCounterAccumulator.
    *
    * @param name the name of the accumulator
    */
   public SparkMMultiCounterAccumulator(String name) {
      super(new LocalMMultiCounterAccumulator<>(name));
   }

   private LocalMMultiCounterAccumulator<K1, K2> getAccumulator() {
      return Cast.as(Cast.<AccumulatorV2Wrapper>as(accumulatorV2).accumulator);
   }


   @Override
   public void merge(MultiCounter<K1, K2> other) {
      getAccumulator().merge(other);
   }

   @Override
   public void increment(K1 firstKey, K2 secondKey) {
      getAccumulator().increment(firstKey, secondKey);
   }

   @Override
   public void increment(K1 firstKey, K2 secondKey, double value) {
      getAccumulator().increment(firstKey, secondKey, value);
   }

}//END OF SparkMapAccumulator
