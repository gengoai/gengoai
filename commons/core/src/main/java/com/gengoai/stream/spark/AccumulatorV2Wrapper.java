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

import com.gengoai.conversion.Cast;
import com.gengoai.stream.local.LocalMAccumulator;
import org.apache.spark.util.AccumulatorV2;

import java.io.Serializable;
import java.util.Optional;

/**
 * Wraps a {@link LocalMAccumulator} making it usable by Spark
 *
 * @param <IN>  the type parameter for what is being accumulated
 * @param <OUT> the type parameter for the result of the accumulation
 * @author David B. Bracewell
 */
public class AccumulatorV2Wrapper<IN, OUT> extends AccumulatorV2<IN, OUT> implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The Accumulator.
    */
   public final LocalMAccumulator<IN, OUT> accumulator;

   /**
    * Instantiates a new AccumulatorV2Wrapper.
    *
    * @param accumulator the accumulator to wrap
    */
   public AccumulatorV2Wrapper(LocalMAccumulator<IN, OUT> accumulator) {
      this.accumulator = accumulator;
   }

   @Override
   public AccumulatorV2<IN, OUT> copyAndReset() {
      AccumulatorV2<IN, OUT> accumulator = copy();
      accumulator.reset();
      return accumulator;
   }

   @Override
   public void add(IN v) {
      accumulator.add(v);
   }

   @Override
   public AccumulatorV2<IN, OUT> copy() {
      return new AccumulatorV2Wrapper<>(accumulator.copy());
   }

   @Override
   public boolean isZero() {
      return accumulator.isZero();
   }

   @Override
   public void merge(AccumulatorV2<IN, OUT> other) {
      if (other instanceof AccumulatorV2Wrapper) {
         accumulator.merge(Cast.<AccumulatorV2Wrapper<IN, OUT>>as(other).accumulator);
      } else {
         throw new IllegalArgumentException(getClass().getSimpleName() + " cannot merge with " + other.getClass()
                                                                                                      .getSimpleName());
      }
   }

   /**
    * Gets the name of the wrapped accumulator
    *
    * @return the name of the wrapped accumulator as an optional
    */
   public Optional<String> getWrappedName() {
      return accumulator.name();
   }


   @Override
   public void reset() {
      accumulator.reset();
   }

   @Override
   public OUT value() {
      return accumulator.value();
   }


}// END OF AccumulatorV2Wrapper
