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

package com.gengoai.stream.local;

import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.stream.MAccumulator;
import com.gengoai.stream.MCounterAccumulator;

/**
 * <p>An implementation of a {@link MCounterAccumulator} for local streams</p>
 *
 * @param <IN> the component type parameter of the counter
 * @author David B. Bracewell
 */
public class LocalMCounterAccumulator<IN> extends LocalMAccumulator<IN, Counter<IN>> implements MCounterAccumulator<IN> {
   private static final long serialVersionUID = 1L;
   private final Counter<IN> counter = Counters.newConcurrentCounter();

   /**
    * Instantiates a new LocalMCounterAccumulator.
    *
    * @param name the name of the accumulator
    */
   public LocalMCounterAccumulator(String name) {
      super(name);
   }

   @Override
   public void add(IN in) {
      counter.increment(in);
   }

   @Override
   public void merge(MAccumulator<IN, Counter<IN>> other) {
      if (other instanceof LocalMAccumulator) {
         this.counter.merge(other.value());
      } else {
         throw new IllegalArgumentException(getClass().getName() + " cannot merge with " + other.getClass().getName());
      }
   }

   @Override
   public void reset() {
      counter.clear();
   }

   @Override
   public Counter<IN> value() {
      return counter;
   }

   @Override
   public boolean isZero() {
      return counter.isEmpty();
   }

   @Override
   public LocalMAccumulator<IN, Counter<IN>> copy() {
      LocalMCounterAccumulator<IN> copy = new LocalMCounterAccumulator<>(name().orElse(null));
      copy.counter.merge(counter);
      return copy;
   }

   @Override
   public void increment(IN item, double amount) {
      counter.increment(item, amount);
   }

   @Override
   public void merge(Counter<? extends IN> counter) {
      this.counter.merge(counter);
   }

}// END OF LocalMCounterAccumulator
