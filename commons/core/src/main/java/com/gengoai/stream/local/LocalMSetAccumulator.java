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


import com.gengoai.collection.Sets;
import com.gengoai.stream.MAccumulator;

import java.util.Set;

/**
 * <p>An implementation of a {@link MAccumulator} that accumulates values in a set for local streams</p>
 *
 * @author David B. Bracewell
 */
public class LocalMSetAccumulator<E> extends LocalMAccumulator<E, Set<E>> {
   private static final long serialVersionUID = 1L;
   private final Set<E> items = Sets.newConcurrentHashSet();

   /**
    * Instantiates a LocalMIndexAccumulator.
    *
    * @param name the name of the accumulator (null is ok)
    */
   public LocalMSetAccumulator(String name) {
      super(name);
   }

   @Override
   public void add(E e) {
      items.add(e);
   }

   @Override
   public void merge(MAccumulator<E, Set<E>> other) {
      if (other instanceof LocalMAccumulator) {
         this.items.addAll(other.value());
      } else {
         throw new IllegalArgumentException(getClass().getName() + " cannot merge with " + other.getClass().getName());
      }
   }

   @Override
   public void reset() {
      items.clear();
   }

   @Override
   public Set<E> value() {
      return items;
   }

   @Override
   public boolean isZero() {
      return items.isEmpty();
   }

   @Override
   public LocalMAccumulator<E, Set<E>> copy() {
      LocalMSetAccumulator<E> copy = new LocalMSetAccumulator<>(name().orElse(null));
      copy.items.addAll(items);
      return copy;
   }

}//END OF LocalMSetAccumulator
