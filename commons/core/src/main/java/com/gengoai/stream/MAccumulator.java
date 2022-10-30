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

package com.gengoai.stream;


import com.gengoai.function.SerializablePredicate;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * <p>Base interface for accumulators that can work across local and distributed streams.</p>
 *
 * @param <IN>  the type parameter for what is being accumulated
 * @param <OUT> the type parameter for the result of the accumulation
 * @author David B. Bracewell
 */
public interface MAccumulator<IN, OUT> extends Serializable {

   /**
    * Adds an item to the accumulator
    *
    * @param in the item to add
    */
   void add(IN in);

   /**
    * Merges another accumulator with this one
    *
    * @param other the other accumulator to merge
    * @throws NullPointerException     if the other accumulator is null
    * @throws IllegalArgumentException if the other accumulator cannot be merged with this one
    */
   void merge(MAccumulator<IN, OUT> other);

   /**
    * The name of the accumulator
    *
    * @return the optional name of the accumulator
    */
   Optional<String> name();

   /**
    * Resets the accumulator to its zero-value.
    */
   void reset();

   /**
    * The value of the accumulator.
    *
    * @return the result of the accumulator
    */
   OUT value();

   /**
    * Determines if the accumulator is a zero value
    *
    * @return True if the accumulator is in a zero state
    */
   boolean isZero();

   /**
    * Registers the accumulator.
    */
   void register();

   /**
    * Reports the given message when the given predicate evaluates to true.
    *
    * @param when    the predicate controlling when the message should be performed
    * @param message a consumer processing the current value of the accumulator
    */
   default void report(SerializablePredicate<? super OUT> when, Consumer<OUT> message) {

   }

}// END OF MAcc
