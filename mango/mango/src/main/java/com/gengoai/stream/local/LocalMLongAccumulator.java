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

import com.gengoai.stream.MAccumulator;
import com.gengoai.stream.MLongAccumulator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>An implementation of a {@link MLongAccumulator} for local streams</p>
 *
 * @author David B. Bracewell
 */
public class LocalMLongAccumulator extends LocalMAccumulator<Long, Long> implements MLongAccumulator {
   private static final long serialVersionUID = 1L;
   private final AtomicLong longValue;


   /**
    * Instantiates a new Local m long accumulator.
    *
    * @param longValue the initial long value
    * @param name      the name of the accumulator
    */
   public LocalMLongAccumulator(long longValue, String name) {
      super(name);
      this.longValue = new AtomicLong(longValue);
   }


   @Override
   public LocalMAccumulator<Long, Long> copy() {
      return new LocalMLongAccumulator(longValue.get(), name().orElse(null));
   }

   @Override
   public void add(long value) {
      longValue.addAndGet(value);
   }

   @Override
   public Long value() {
      return longValue.longValue();
   }

   @Override
   public boolean isZero() {
      return longValue.get() == 0;
   }

   @Override
   public void merge(MAccumulator<Long, Long> other) {
      if (other instanceof LocalMAccumulator) {
         longValue.addAndGet(other.value());
      } else {
         throw new IllegalArgumentException(getClass().getName() + " cannot merge with " + other.getClass().getName());
      }
   }

   @Override
   public void reset() {
      longValue.set(0);
   }

}// END OF LocalMLongAccumulator
