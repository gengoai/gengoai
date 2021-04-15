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

import com.gengoai.collection.counter.Counter;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.MCounterAccumulator;
import com.gengoai.stream.local.LocalMCounterAccumulator;

/**
 * <p>Counter accumulator implementation for Spark streams</p>
 *
 * @param <E> the component type parameter of the cunter
 * @author David B. Bracewell
 */
public class SparkMCounterAccumulator<E> extends SparkMAccumulator<E, Counter<E>> implements MCounterAccumulator<E> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Spark m counter accumulator.
    *
    * @param name the name of the accumulator
    */
   public SparkMCounterAccumulator(String name) {
      super(new LocalMCounterAccumulator<>(name));
   }

   private LocalMCounterAccumulator<E> getAccumulator() {
      return Cast.as(Cast.<AccumulatorV2Wrapper>as(accumulatorV2).accumulator);
   }

   @Override
   public void increment(E item, double amount) {
      getAccumulator().increment(item, amount);
   }

   @Override
   public void merge(Counter<? extends E> counter) {
      getAccumulator().merge(counter);
   }

}//END OF SparkMCounterAccumulator
