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

import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.collection.counter.MultiCounters;
import com.gengoai.stream.MAccumulator;
import com.gengoai.stream.MMultiCounterAccumulator;
import com.gengoai.tuple.Tuple2;

/**
 * <p>An implementation of a {@link MMultiCounterAccumulator} for local streams</p>
 *
 * @param <K1> the first key type parameter of the MultiCounter
 * @param <K2> the second key type parameter of the MultiCounter
 * @author David B. Bracewell
 */
public class LocalMMultiCounterAccumulator<K1, K2> extends LocalMAccumulator<Tuple2<K1, K2>, MultiCounter<K1, K2>> implements MMultiCounterAccumulator<K1, K2> {
   private static final long serialVersionUID = 1L;
   private final MultiCounter<K1, K2> counter = MultiCounters.newConcurrentMultiCounter();

   /**
    * Instantiates a new LocalMMultiCounterAccumulator.
    *
    * @param name the name of the accumulator
    */
   public LocalMMultiCounterAccumulator(String name) {
      super(name);
   }

   @Override
   public void add(Tuple2<K1, K2> objects) {
      counter.increment(objects.v1, objects.v2);
   }

   @Override
   public LocalMAccumulator<Tuple2<K1, K2>, MultiCounter<K1, K2>> copy() {
      LocalMMultiCounterAccumulator<K1, K2> copy = new LocalMMultiCounterAccumulator<>(name().orElse(null));
      copy.counter.merge(counter);
      return copy;
   }

   @Override
   public MultiCounter<K1, K2> value() {
      return counter;
   }

   @Override
   public boolean isZero() {
      return counter.isEmpty();
   }

   @Override
   public void merge(MAccumulator<Tuple2<K1, K2>, MultiCounter<K1, K2>> other) {
      if (other instanceof LocalMAccumulator) {
         counter.merge(other.value());
      } else {
         throw new IllegalArgumentException(getClass().getName() + " cannot merge with " + other.getClass().getName());
      }
   }

   @Override
   public void reset() {
      counter.clear();
   }

   @Override
   public void increment(K1 firstKey, K2 secondKey) {
      counter.increment(firstKey, secondKey);
   }

   @Override
   public void increment(K1 firstKey, K2 secondKey, double value) {
      counter.increment(firstKey, secondKey, value);
   }

   @Override
   public void merge(MultiCounter<K1, K2> other) {
      this.counter.merge(other);
   }

}//END OF LocalMMultiCounterAccumulator
