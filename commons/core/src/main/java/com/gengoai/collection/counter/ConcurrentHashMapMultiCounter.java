/*
 * (c) 2005 David B. Bracewell
 *
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

package com.gengoai.collection.counter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.tuple.Tuple3;
import lombok.NonNull;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of a MultiCounter using a ConcurrentHashMaps with {@link ConcurrentHashMapCounter} as the child
 * counters..
 *
 * @param <K> the first key type
 * @param <V> the second type
 * @author David B. Bracewell
 */
@JsonDeserialize(as = ConcurrentHashMapMultiCounter.class)
public class ConcurrentHashMapMultiCounter<K, V> extends BaseMultiCounter<K, V> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new ConcurrentHashMapMultiCounter initializing it with the given values.
    *
    * @param items the items
    */
   @JsonCreator
   public ConcurrentHashMapMultiCounter(@JsonProperty @NonNull Collection<Tuple3<K, V, Double>> items) {
      this();
      items.forEach(t -> set(t.v1, t.v2, t.v3));
   }

   /**
    * Instantiates a new ConcurrentHashMapMultiCounter.
    */
   public ConcurrentHashMapMultiCounter() {
      super(new ConcurrentHashMap<>());
   }

   @Override
   protected Counter<V> createCounter() {
      return new ConcurrentHashMapCounter<>();
   }

   @Override
   protected MultiCounter<K, V> newInstance() {
      return new ConcurrentHashMapMultiCounter<>();
   }

}//END OF HashMapMultiCounter
