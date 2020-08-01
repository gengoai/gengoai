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

import com.gengoai.collection.counter.Counter;

/**
 * Accumulator for {@link Counter}s
 *
 * @param <T> the component type parameter of the counter
 * @author David B. Bracewell
 */
public interface MCounterAccumulator<T> extends MAccumulator<T, Counter<T>> {

   /**
    * Increments the given item by the given amount.
    *
    * @param item   the item to increment
    * @param amount the amount to increment the item by
    */
   void increment(T item, double amount);

   /**
    * Merges the given counter with this accumulator
    *
    * @param counter the counter to merge
    */
   void merge(Counter<? extends T> counter);

}// END OF MCounterAccumulator
