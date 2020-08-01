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

import com.gengoai.Copyable;
import com.gengoai.function.SerializablePredicate;
import com.gengoai.stream.MAccumulator;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * <p>Abstract base for local accumulators.</p>
 *
 * @param <IN>  the type parameter for what is being accumulated
 * @param <OUT> the type parameter for the result of the accumulation
 * @author David B. Bracewell
 */
public abstract class LocalMAccumulator<IN, OUT> implements MAccumulator<IN, OUT>, Copyable<LocalMAccumulator<IN, OUT>> {
   private static final long serialVersionUID = 1L;
   private final String name;

   /**
    * Instantiates a new Local m accumulator.
    *
    * @param name the name of the accumulator (null is ok)
    */
   protected LocalMAccumulator(String name) {
      this.name = name;
   }

   @Override
   public final Optional<String> name() {
      return Optional.ofNullable(name);
   }

   @Override
   public void register() {

   }

   @Override
   public void report(SerializablePredicate<? super OUT> when, Consumer<OUT> message) {
      if (when.test(value())) {
         message.accept(value());
      }
   }

}//END OF LocalMAccumulator
