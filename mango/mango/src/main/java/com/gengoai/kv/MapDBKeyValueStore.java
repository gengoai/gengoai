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

import com.gengoai.collection.disk.NavigableDiskMap;
import com.gengoai.io.resource.Resource;

import java.io.File;
import java.util.NavigableMap;

/**
 * @author David B. Bracewell
 */
class MapDBKeyValueStore<K, V> extends AbstractNavigableKeyValueStore<K, V> {
   private static final long serialVersionUID = 1L;
   private final NavigableDiskMap<K, V> map;

   public MapDBKeyValueStore(Resource dbFile, String namespace, boolean compressed, boolean readOnly) {
      super(namespace, readOnly);
      this.map = NavigableDiskMap.<K, V>builder()
         .file(dbFile)
         .namespace(namespace)
         .compressed(compressed)
         .readOnly(readOnly)
         .build();
   }

   @Override
   public void close() throws Exception {
      map.close();
   }

   @Override
   public void commit() {
      map.commit();
   }


   @Override
   protected NavigableMap<K, V> delegate() {
      return map;
   }
}//END OF MVKeyValueStore
