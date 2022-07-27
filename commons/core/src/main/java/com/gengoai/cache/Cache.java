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
package com.gengoai.cache;


import com.gengoai.function.SerializableFunction;
import com.gengoai.function.SerializableSupplier;
import lombok.NonNull;

/**
 * <p>A generic cache interface. A cache represents a memorized key-value pair.</p>
 *
 * @param <K> the Key parameter
 * @param <V> the Value parameter
 * @author David B. Bracewell
 */
public interface Cache<K, V> {

   /**
    * Creates an LRU cache.
    *
    * @param <K>     the key type parameter
    * @param <V>     the value type parameter
    * @param maxSize the max size of the cache
    * @return the cache
    */
   static <K, V> Cache<K, V> create(int maxSize) {
      return new LRUCache<>(maxSize);
   }

   /**
    * Creates an Auto Calculating LRU cache.
    *
    * @param <K>             the key type parameter
    * @param <V>             the value type parameter
    * @param maxSize         the max size of the cache
    * @param valueCalculator the value calculator to use when getting the value for a key
    * @return the cache
    */
   static <K, V> Cache<K, V> create(int maxSize, @NonNull SerializableFunction<K, V> valueCalculator) {
      return new AutoCalculatingLRUCache<>(maxSize, valueCalculator);
   }

   /**
    * Determines if a key is in the cache or not
    *
    * @param key The key to check
    * @return True if the key is in the cache, False if not
    */
   boolean containsKey(K key);

   /**
    * Gets the value associated with a key
    *
    * @param key The key
    * @return The value associated with the key or null
    */
   V get(K key);

   /**
    * Gets the value associated with the given key when available and if not available calculates and stores the value
    * using the given supplier.
    *
    * @param key      The key
    * @param supplier The supplier to use to generate the value
    * @return The old value if put, null if not
    */
   V get(K key, SerializableSupplier<? extends V> supplier);

   /**
    * Removes a single key
    *
    * @param key The key to remove
    */
   void invalidate(K key);

   /**
    * Clears the cache
    */
   void invalidateAll();

   /**
    * Clears the cache of all given keys
    *
    * @param keys The keys to remove
    */
   default void invalidateAll(Iterable<? extends K> keys) {
      keys.forEach(this::invalidate);
   }

   /**
    * Determines if the cache is empty or not
    *
    * @return True if empty, False if not
    */
   default boolean isEmpty() {
      return size() == 0;
   }

   /**
    * Adds a key value pair to the cache overwriting any value that is there
    *
    * @param key   The key
    * @param value The value
    */
   void put(K key, V value);

   /**
    * The number of items cached.
    *
    * @return The current size of the cache
    */
   long size();


}//END OF Cache
