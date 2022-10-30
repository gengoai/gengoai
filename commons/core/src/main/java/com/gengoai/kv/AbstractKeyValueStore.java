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

import com.gengoai.Validation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * The type Abstract navigable key value store.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public abstract class AbstractKeyValueStore<K, V> implements KeyValueStore<K, V>, Serializable {
   private static final long serialVersionUID = 1L;
   private final String namespace;
   private final boolean readOnly;

   /**
    * Instantiates a new Abstract navigable key value store.
    *
    * @param namespace the namespace
    * @param readOnly  the read only
    */
   protected AbstractKeyValueStore(String namespace, boolean readOnly) {
      this.namespace = Validation.notNullOrBlank(namespace, "Namespace must not be null or blank");
      this.readOnly = readOnly;
   }

   @Override
   public void clear() {
      delegate().clear();
   }

   @Override
   public boolean containsKey(Object o) {
      return delegate().containsKey(o);
   }

   @Override
   public boolean containsValue(Object o) {
      return delegate().containsValue(o);
   }

   /**
    * Delegate navigable map.
    *
    * @return the navigable map
    */
   protected abstract Map<K, V> delegate();

   @Override
   public Set<Entry<K, V>> entrySet() {
      return delegate().entrySet();
   }

   @Override
   public V get(Object o) {
      return delegate().get(o);
   }

   @Override
   public String getNameSpace() {
      return namespace;
   }

   @Override
   public boolean isEmpty() {
      return delegate().isEmpty();
   }

   @Override
   public boolean isReadOnly() {
      return readOnly;
   }


   @Override
   public Set<K> keySet() {
      return delegate().keySet();
   }


   @Override
   public V put(K k, V v) {
      return delegate().put(k, v);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> map) {
      delegate().putAll(map);
   }

   @Override
   public V remove(Object o) {
      return delegate().remove(o);
   }

   @Override
   public int size() {
      return delegate().size();
   }

   @Override
   public long sizeAsLong() {
      return size();
   }

   @Override
   public Collection<V> values() {
      return delegate().values();
   }
}//END OF InMemoryKeyValueStore
