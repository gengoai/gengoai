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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>An implementation of a {@link MAccumulator} that accumulates values in lists for local streams</p>
 *
 * @param <E> the component type parameter of the list
 * @author David B. Bracewell
 */
public class LocalMListAccumulator<E> extends LocalMAccumulator<E, List<E>> {
   private static final long serialVersionUID = 1L;
   private final CopyOnWriteArrayList<E> list = new CopyOnWriteArrayList<>();

   /**
    * Instantiates a new LocalMListAccumulator.
    *
    * @param name the name of the accumulator
    */
   public LocalMListAccumulator(String name) {
      super(name);
   }

   @Override
   public void add(E e) {
      list.add(e);
   }

   @Override
   public void merge(MAccumulator<E, List<E>> other) {
      if (other instanceof LocalMAccumulator) {
         this.list.addAll(other.value());
      } else {
         throw new IllegalArgumentException(getClass().getName() + " cannot merge with " + other.getClass().getName());
      }
   }

   @Override
   public void reset() {
      list.clear();
   }

   @Override
   public List<E> value() {
      return list;
   }

   @Override
   public boolean isZero() {
      return list.isEmpty();
   }

   @Override
   public LocalMAccumulator<E, List<E>> copy() {
      LocalMListAccumulator<E> copy = new LocalMListAccumulator<>(name().orElse(null));
      copy.list.addAll(list);
      return copy;
   }
}//END OF LocalMListAccumulator
