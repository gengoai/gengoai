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
import java.util.*;

/**
 * <p>A NavigableMap implementation that stores its data in a file. NavigableDiskMap are monitored to ensure that their
 * file handle is properly closed if the it is no longer in use. However, one can use the <code>commit</code> and
 * <code>close</code> methods to explicitly commit changes and close the map.</p>
 * <p>NavigableDiskMap are created using a builder in the following way:</p>
 * <pre>
 * {@code
 *  var map = NavigableDiskMap.builder()
 *                   .file(Resources.from("/data/map.db")
 *                   .namespace("people")
 *                   .compressed(true)
 *                   .build();
 * }
 * </pre>
 * <p>Once an NavigableDiskMap instance is constructed it acts like regular Java NavigableMap.</p>
 *
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 */
public final class NavigableDiskMap<K, V> implements NavigableMap<K, V>, Serializable, AutoCloseable {
   private static final long serialVersionUID = 1L;
   private final MonitoredObject<MapDBHandle> handle;
   @Getter
   private final String nameSpace;
   @Getter
   private final boolean readOnly;
   private volatile transient NavigableMap<K, V> map;


   @Builder
   private NavigableDiskMap(@NonNull Resource file, String namespace, boolean compressed, boolean readOnly) {
      this.nameSpace = Validation.notNullOrBlank(namespace);
      this.handle = ResourceMonitor.monitor(new MapDBHandle(file, compressed));
      this.readOnly = readOnly;
   }

   @Override
   public Entry<K, V> ceilingEntry(K k) {
      return delegate().ceilingEntry(k);
   }

   @Override
   public K ceilingKey(K k) {
      return delegate().ceilingKey(k);
   }

   @Override
   public void clear() {
      delegate().clear();
   }

   @Override
   public void close() throws Exception {
      handle.object.close();
   }

   /**
    * Commit.
    */
   public void commit() {
      handle.object.commit();
   }

   @Override
   public Comparator<? super K> comparator() {
      return delegate().comparator();
   }

   @Override
   public boolean containsKey(Object o) {
      return delegate().containsKey(o);
   }

   @Override
   public boolean containsValue(Object o) {
      return delegate().containsValue(o);
   }

   private NavigableMap<K, V> delegate() {
      if (map == null || handle.object.isClosed()) {
         synchronized (this) {
            if (map == null || handle.object.isClosed()) {
               map = this.handle.object.getStore().getTreeMap(getNameSpace());
               if (isReadOnly()) {
                  map = Collections.unmodifiableNavigableMap(map);
               }
            }
         }
      }
      return map;
   }

   @Override
   public NavigableSet<K> descendingKeySet() {
      return delegate().descendingKeySet();
   }

   @Override
   public NavigableMap<K, V> descendingMap() {
      return delegate().descendingMap();
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      return delegate().entrySet();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof NavigableDiskMap)) return false;
      NavigableDiskMap<?, ?> that = (NavigableDiskMap<?, ?>) o;
      return isReadOnly() == that.isReadOnly() &&
            Objects.equals(handle, that.handle) &&
            Objects.equals(getNameSpace(), that.getNameSpace());
   }

   @Override
   public Entry<K, V> firstEntry() {
      return delegate().firstEntry();
   }

   @Override
   public K firstKey() {
      return delegate().firstKey();
   }

   @Override
   public Entry<K, V> floorEntry(K k) {
      return delegate().floorEntry(k);
   }

   @Override
   public K floorKey(K k) {
      return delegate().floorKey(k);
   }

   @Override
   public V get(Object o) {
      return delegate().get(o);
   }

   public MapDBHandle getHandle() {
      return handle.object;
   }

   @Override
   public int hashCode() {
      return Objects.hash(handle, getNameSpace(), isReadOnly());
   }

   @Override
   public NavigableMap<K, V> headMap(K k, boolean b) {
      return delegate().headMap(k, b);
   }

   @Override
   public SortedMap<K, V> headMap(K k) {
      return delegate().headMap(k);
   }

   @Override
   public Entry<K, V> higherEntry(K k) {
      return delegate().higherEntry(k);
   }

   @Override
   public K higherKey(K k) {
      return delegate().higherKey(k);
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
   public Entry<K, V> lastEntry() {
      return delegate().lastEntry();
   }

   @Override
   public K lastKey() {
      return delegate().lastKey();
   }

   @Override
   public Entry<K, V> lowerEntry(K k) {
      return delegate().lowerEntry(k);
   }

   @Override
   public K lowerKey(K k) {
      return delegate().lowerKey(k);
   }

   @Override
   public NavigableSet<K> navigableKeySet() {
      return delegate().navigableKeySet();
   }

   @Override
   public Entry<K, V> pollFirstEntry() {
      return delegate().pollFirstEntry();
   }

   @Override
   public Entry<K, V> pollLastEntry() {
      return delegate().pollLastEntry();
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
   public NavigableMap<K, V> subMap(K k, boolean b, K k1, boolean b1) {
      return delegate().subMap(k, b, k1, b1);
   }

   @Override
   public SortedMap<K, V> subMap(K k, K k1) {
      return delegate().subMap(k, k1);
   }

   @Override
   public NavigableMap<K, V> tailMap(K k, boolean b) {
      return delegate().tailMap(k, b);
   }

   @Override
   public SortedMap<K, V> tailMap(K k) {
      return delegate().tailMap(k);
   }

   @Override
   public String toString() {
      return map.toString();
   }

   @Override
   public Collection<V> values() {
      return delegate().values();
   }

}//END OF NavigableDiskMap
