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

import lombok.NonNull;

import java.util.Map;

/**
 * A Key-Value store is a database in which data is associated with a unique key providing quick retrieval via the key.
 * Key-Value Stores are created using {@link #connect(String)}  where the specification is
 * defined as follows: <code>kv::[type]::[namespace]::[path];compressed=[true,false];readOnly=[true,false]</code>
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public interface KeyValueStore<K, V> extends Map<K, V>, AutoCloseable {

   static <K, V, T extends KeyValueStore<K, V>> T connect(@NonNull String connectionString) {
      return KeyValueStoreConnection.parse(connectionString).connect();
   }


   /**
    * Commits changes to the key-value store to be persisted
    */
   void commit();

   /**
    * Gets the namespace of the key-value store
    *
    * @return the name space
    */
   String getNameSpace();

   /**
    * Gets the number of keys in the store as a long
    *
    * @return the number of keys in the store
    */
   long sizeAsLong();

   /**
    * Is this key-value store read-only?
    *
    * @return True - read only, False - writeable
    */
   boolean isReadOnly();

}//END OF KeyValueStore
