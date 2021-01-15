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

import java.util.Collections;
import java.util.NavigableMap;

/**
 * Key-Value store implementation that stores all data in memory. Underlying maps are free to be shared across multiple
 * kv stores and will only be garbage collected when there are no longer any references.
 *
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 * @author David B. Bracewell
 */
class InMemoryNavigableKeyValueStore<K, V> extends AbstractNavigableKeyValueStore<K, V> {
   private static final long serialVersionUID = 1L;
   private transient volatile NavigableMap<K, V> map;

   /**
    * Instantiates a new In memory key value store.
    *
    * @param namespace the namespace
    * @param readOnly  the read only
    */
   public InMemoryNavigableKeyValueStore(String namespace, boolean readOnly) {
      super(namespace, readOnly);
      delegate();
   }


   @Override
   public void close() throws Exception {
      map = null;
   }

   @Override
   public void commit() {

   }


   protected NavigableMap<K, V> delegate() {
      if (map == null) {
         synchronized (this) {
            if (map == null) {
               map = MapRegistry.getNavigable(getNameSpace());
               if (isReadOnly()) {
                  map = Collections.unmodifiableNavigableMap(map);
               }
            }
         }
      }
      return map;
   }


}//END OF InMemoryKeyValueStore
