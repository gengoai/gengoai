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

package com.gengoai.collection.disk;

import com.gengoai.Validation;
import com.gengoai.collection.tree.Span;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.io.resource.Resource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.mapdb.BTreeMap;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DiskMap<K, V> implements Map<K, V>, AutoCloseable, Serializable {
   private static final long serialVersionUID = 1L;
   private final MonitoredObject<MapDBHandle> handle;
   @Getter
   private final String nameSpace;
   @Getter
   private final boolean readOnly;
   private volatile transient Map<K, V> map;


   @Builder
   private DiskMap(@NonNull Resource file, String namespace, boolean compressed, boolean readOnly) {
      this.nameSpace = Validation.notNullOrBlank(namespace);
      this.handle = ResourceMonitor.monitor(new MapDBHandle(file, compressed));
      this.readOnly = readOnly;
   }

   @Override
   public void clear() {
      map.clear();
   }

   @Override
   public void close() throws Exception {
      handle.object.close();
   }

   public void commit() {
      handle.object.commit();
   }

   @Override
   public boolean containsKey(Object o) {
      return delegate().containsKey(o);
   }

   @Override
   public boolean containsValue(Object o) {
      return delegate().containsValue(o);
   }

   private Map<K, V> delegate() {
      if (map == null) {
         synchronized (this) {
            if (map == null) {
               map = this.handle.object.getStore().getHashMap(getNameSpace());
               if (isReadOnly()) {
                  map = Collections.unmodifiableMap(map);
               }
            }
         }
      }
      return map;
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      return map.entrySet();
   }

   @Override
   public V get(Object o) {
      return delegate().get(o);
   }

   public MapDBHandle getHandle() {
      return handle.object;
   }

   @Override
   public boolean isEmpty() {
      return delegate().isEmpty();
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
   public Collection<V> values() {
      return delegate().values();
   }

}//END OF DiskMap
