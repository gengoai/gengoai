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
import com.gengoai.stream.MDoubleAccumulator;

import java.util.concurrent.atomic.DoubleAdder;

/**
 * <p>An implementation of a {@link MDoubleAccumulator} for local streams</p>
 *
 * @author David B. Bracewell
 */
public class LocalMDoubleAccumulator extends LocalMAccumulator<Double, Double> implements MDoubleAccumulator {
   private static final long serialVersionUID = 1L;
   private final DoubleAdder value;

   /**
    * Instantiates a new Local double accumulator.
    *
    * @param value the initial value of the accumulator
    * @param name  the name of the accumulator
    */
   public LocalMDoubleAccumulator(double value, String name) {
      super(name);
      this.value = new DoubleAdder();
      this.value.add(value);
   }

   @Override
   public LocalMAccumulator<Double, Double> copy() {
      return new LocalMDoubleAccumulator(value.doubleValue(), name().orElse(null));
   }

   @Override
   public Double value() {
      return value.doubleValue();
   }

   @Override
   public boolean isZero() {
      return value.doubleValue() == 0;
   }

   @Override
   public void merge(MAccumulator<Double, Double> other) {
      if (other instanceof LocalMAccumulator) {
         add(other.value());
      } else {
         throw new IllegalArgumentException(getClass().getName() + " cannot merge with " + other.getClass().getName());
      }
   }

   @Override
   public void reset() {
      value.reset();
   }

   @Override
   public void add(double value) {
      this.value.add(value);
   }

}// END OF LocalMDoubleAccumulator
