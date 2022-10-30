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

package com.gengoai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.stream.Streams;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import static com.gengoai.Validation.notNull;

/**
 * <p>Mimics {@link String#intern()} with any object using heap memory. Uses weak references so that objects no longer
 * in memory can be reclaimed.</p>
 *
 * @param <E> the type parameter
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public final class Interner<E> implements Serializable {
   private static final long serialVersionUID = 1L;
   private volatile WeakHashMap<E, E> map = new WeakHashMap<>();

   /**
    * Instantiates a new Interner initializing it with the given items.
    *
    * @param items the items
    */
   @JsonCreator
   public Interner(@JsonProperty @NonNull Collection<? extends E> items) {
      internAll(items);
   }

   /**
    * <p>Adds or gets the canonical version of the incoming object.</p>
    *
    * @param object The object to intern
    * @return The interned value
    * @throws NullPointerException if the object is null
    */
   public synchronized E intern(final E object) {
      return map.computeIfAbsent(notNull(object), o -> object);
   }

   /**
    * <p>Interns all elements in the given iterable.</p>
    *
    * @param iterable the items of elements to intern.
    * @return the interned elements.
    * @throws NullPointerException if the collection is null
    */
   public Collection<E> internAll(@JsonProperty @NonNull Iterable<? extends E> iterable) {
      return Streams.asStream(notNull(iterable))
                    .map(this::intern)
                    .collect(Collectors.toList());
   }

   @JsonValue
   private Set<E> keySet() {
      return map.keySet();
   }

   /**
    * <p>The number of items that have been interned.</p>
    *
    * @return the number of items that have been interned.
    */
   public int size() {
      return map.size();
   }

   @Override
   public String toString() {
      return "Interner{size=" + size() + "}";
   }

}//END OF Interner
