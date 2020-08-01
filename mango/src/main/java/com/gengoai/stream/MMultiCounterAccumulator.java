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

import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.tuple.Tuple2;

/**
 * An accumulator for {@link MultiCounter}s
 *
 * @param <K1> the first key type parameter
 * @param <K2> the second key type parameter
 * @author David B. Bracewell
 */
public interface MMultiCounterAccumulator<K1, K2> extends MAccumulator<Tuple2<K1, K2>, MultiCounter<K1, K2>> {

   /**
    * Increments the count of the two keys.
    *
    * @param firstKey  the first key
    * @param secondKey the second key
    */
   void increment(K1 firstKey, K2 secondKey);

   /**
    * Increments the count of the two keys by the given value.
    *
    * @param firstKey  the first key
    * @param secondKey the second key
    * @param value     the amount to increment by
    */
   void increment(K1 firstKey, K2 secondKey, double value);

   /**
    * Merges the given MultiCounter with this accumulator
    *
    * @param other the MultiCounter to merge
    */
   void merge(MultiCounter<K1, K2> other);

}//END OF MMultiCounterAccumulator
