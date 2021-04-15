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
 *
 */

package com.gengoai.kv;

import com.gengoai.collection.Maps;

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;

/**
 * The type Abstract navigable key value store.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public abstract class AbstractNavigableKeyValueStore<K, V> extends AbstractKeyValueStore<K, V> implements NavigableKeyValueStore<K, V> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Abstract navigable key value store.
    *
    * @param namespace the namespace
    * @param readOnly  the read only
    */
   protected AbstractNavigableKeyValueStore(String namespace, boolean readOnly) {
      super(namespace, readOnly);
   }

   @Override
   public K ceilingKey(K key) {
      return delegate().ceilingKey(key);
   }

   /**
    * Delegate navigable map.
    *
    * @return the navigable map
    */
   protected abstract NavigableMap<K, V> delegate();

   @Override
   public Set<Entry<K, V>> entrySet() {
      return delegate().entrySet();
   }

   @Override
   public K firstKey() {
      return delegate().firstKey();
   }

   @Override
   public K floorKey(K key) {
      return delegate().floorKey(key);
   }

   @Override
   public K higherKey(K key) {
      return delegate().higherKey(key);
   }

   @Override
   public Iterator<K> keyIterator(K key) {
      return Maps.tailKeyIterator(delegate(), key);
   }


   @Override
   public K lastKey() {
      return delegate().lastKey();
   }

   @Override
   public K lowerKey(K key) {
      return delegate().lowerKey(key);
   }

}//END OF InMemoryKeyValueStore
