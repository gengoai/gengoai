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
import com.gengoai.tuple.Tuple3;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;

/**
 * Implementation of a MultiCounter using a HashMaps.
 *
 * @param <K> the first key type
 * @param <V> the second type
 * @author David B. Bracewell
 */
public class HashMapMultiCounter<K, V> extends BaseMultiCounter<K, V> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new HashMapMultiCounter.
    */
   public HashMapMultiCounter() {
      super(new HashMap<>());
   }

   /**
    * Instantiates a new HashMapMultiCounter initializing it with the given values.
    *
    * @param items the items
    */
   @JsonCreator
   public HashMapMultiCounter(@JsonProperty @NonNull Collection<Tuple3<K, V, Double>> items) {
      this();
      items.forEach(t -> set(t.v1, t.v2, t.v3));
   }

   @Override
   protected Counter<V> createCounter() {
      return new HashMapCounter<>();
   }

   @Override
   protected MultiCounter<K, V> newInstance() {
      return new HashMapMultiCounter<>();
   }

}//END OF HashMapMultiCounter
