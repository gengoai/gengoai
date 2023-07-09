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
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.io.resource.Resource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>A Map implementation that stores its data in a file. Each map file can contain multiple maps where each map is
 * stored in a namespace represented by a unique String name. DiskMaps are monitored to ensure that their file handle is
 * properly closed if the it is no longer in use. However, one can use the <code>commit</code> and <code>close</code>
 * methods to explicitly commit changes and close the map.</p>
 * <p>DiskMaps are created using a builder in the following way:</p>
 * <pre>
 * {@code
 *  var map = DiskMap.builder()
 *                   .file(Resources.from("/data/map.db")
 *                   .namespace("people")
 *                   .compressed(true)
 *                   .build();
 * }
 * </pre>
 * <p>Once an DiskMap instance is constructed it acts like regular Java Map.</p>
 *
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 */
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

   /**
    * Commits any changes made to disk.
    */
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
      return delegate().entrySet();
   }

   @Override
   public V get(Object o) {
      return delegate().get(o);
   }

   /**
    * Gets the {@link MapDBHandle} that manages the map database
    *
    * @return the handle
    */
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
