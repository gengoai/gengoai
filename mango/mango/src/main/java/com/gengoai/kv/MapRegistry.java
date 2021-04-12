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

import com.gengoai.conversion.Cast;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author David B. Bracewell
 */
final class MapRegistry {
   private MapRegistry() {
      throw new IllegalAccessError();
   }

   @SuppressWarnings("rawtypes")
   private static final Map<String, WeakReference<Map>> stores = new ConcurrentHashMap<>();

   static <K, V> ConcurrentSkipListMap<K, V> getNavigable(String namespace) {
      return Cast.as(stores.compute(namespace, (ns, map) -> map == null || map.get() == null
                                                            ? new WeakReference<>(new ConcurrentSkipListMap<>())
                                                            : map).get());
   }

   static <K, V> Map<K, V> get(String namespace) {
      return Cast.as(stores.compute(namespace, (ns, map) -> map == null || map.get() == null
                                                            ? new WeakReference<>(new ConcurrentHashMap<>())
                                                            : map).get());
   }

}//END OF InMemoryKVRegistry
