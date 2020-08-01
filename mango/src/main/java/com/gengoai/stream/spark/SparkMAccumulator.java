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
import com.gengoai.stream.MAccumulator;
import org.apache.spark.util.AccumulatorV2;
import scala.runtime.AbstractFunction0;

import java.util.Optional;

/**
 * <p>Generic accumulator for distributed Spark streams.</p>
 *
 * @param <IN>  the type parameter for what is being accumulated
 * @param <OUT> the type parameter for the result of the accumulation
 * @author David B. Bracewell
 */
public class SparkMAccumulator<IN, OUT> implements MAccumulator<IN, OUT> {
   private static final long serialVersionUID = 1L;
   /**
    * The AccumulatorV2 being used.
    */
   protected final AccumulatorV2<IN, OUT> accumulatorV2;

   /**
    * Instantiates a new Spark m accumulator.
    *
    * @param accumulatorV2 the accumulator v 2
    */
   public SparkMAccumulator(AccumulatorV2<IN, OUT> accumulatorV2) {
      this.accumulatorV2 = accumulatorV2;
   }

   /**
    * Instantiates a new Spark m accumulator by wrapping a local accumulator.
    *
    * @param localMAccumulator the local accumulator to wrap
    */
   public SparkMAccumulator(LocalMAccumulator<IN, OUT> localMAccumulator) {
      this.accumulatorV2 = new AccumulatorV2Wrapper<>(localMAccumulator);
   }

   @Override
   public void add(IN in) {
      accumulatorV2.add(in);
   }


   @Override
   public void merge(MAccumulator<IN, OUT> other) {
      if (other instanceof SparkMAccumulator) {
         accumulatorV2.merge(Cast.<SparkMAccumulator<IN, OUT>>as(other).accumulatorV2);
      }
      throw new IllegalArgumentException(getClass().getName() + " cannot merge with " + other.getClass().getName());
   }

   @Override
   public Optional<String> name() {
      try {
         return Optional.ofNullable(accumulatorV2.name().getOrElse(new AbstractFunction0<String>() {
            @Override
            public String apply() {
               return null;
            }
         }));
      } catch (IllegalAccessError e) {
         return Optional.empty();
      }
   }

   @Override
   public void reset() {
      accumulatorV2.reset();
   }

   @Override
   public OUT value() {
      return accumulatorV2.value();
   }

   @Override
   public boolean isZero() {
      return accumulatorV2.isZero();
   }

   @Override
   public void register() {
      if (accumulatorV2.isRegistered()) {
         return;
      }
      String name;
      if (accumulatorV2 instanceof AccumulatorV2Wrapper) {
         name = Cast.<AccumulatorV2Wrapper<IN, OUT>>as(accumulatorV2).getWrappedName().orElse(null);
      } else {
         name = name().orElse(null);
      }
      if (name == null) {
         SparkStreamingContext.INSTANCE.sparkContext().sc().register(accumulatorV2);
      } else {
         SparkStreamingContext.INSTANCE.sparkContext().sc().register(accumulatorV2, name);
      }
   }

}//END OF SparkMAccumulator
