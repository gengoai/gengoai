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
import com.gengoai.stream.MMapAccumulator;
import com.gengoai.tuple.Tuple2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>An implementation of a {@link MMapAccumulator} for local streams</p>
 *
 * @param <K> the key type parameter of the map
 * @param <V> the value type parameter of the map
 * @author David B. Bracewell
 */
public class LocalMMapAccumulator<K, V> extends LocalMAccumulator<Tuple2<K, V>, Map<K, V>> implements MMapAccumulator<K, V> {
   private static final long serialVersionUID = 1L;
   private final Map<K, V> map = new ConcurrentHashMap<>();


   /**
    * Instantiates a new LocalMMapAccumulator.
    *
    * @param name the name of the accumulator
    */
   public LocalMMapAccumulator(String name) {
      super(name);
   }

   @Override
   public void add(Tuple2<K, V> objects) {
      map.put(objects.v1, objects.v2);
   }

   @Override
   public LocalMAccumulator<Tuple2<K, V>, Map<K, V>> copy() {
      LocalMMapAccumulator<K, V> copy = new LocalMMapAccumulator<>(name().orElse(null));
      copy.putAll(this.map);
      return copy;
   }

   @Override
   public Map<K, V> value() {
      return map;
   }

   @Override
   public boolean isZero() {
      return map.isEmpty();
   }

   @Override
   public void merge(MAccumulator<Tuple2<K, V>, Map<K, V>> other) {
      if (other instanceof LocalMAccumulator) {
         map.putAll(other.value());
      } else {
         throw new IllegalArgumentException(getClass().getName() + " cannot merge with " + other.getClass().getName());
      }
   }


   @Override
   public void reset() {
      map.clear();
   }

   @Override
   public void put(K key, V value) {
      map.put(key, value);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> other) {
      map.putAll(other);
   }

}//END OF LocalMMapAccumulator
