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

import com.gengoai.collection.Sorting;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;

import static com.gengoai.tuple.Tuples.$;

/**
 * The interface Navigable key value store.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public interface NavigableKeyValueStore<K, V> extends KeyValueStore<K, V> {
   /**
    * Ceiling key k.
    *
    * @param key the key
    * @return the k
    */
   K ceilingKey(K key);

   /**
    * First key k.
    *
    * @return the k
    */
   K firstKey();

   /**
    * Floor key k.
    *
    * @param key the key
    * @return the k
    */
   K floorKey(K key);

   /**
    * Higher key k.
    *
    * @param key the key
    * @return the k
    */
   K higherKey(K key);

   /**
    * Key iterator iterator.
    *
    * @param key the key
    * @return the iterator
    */
   Iterator<K> keyIterator(K key);

   /**
    * Last key k.
    *
    * @return the k
    */
   K lastKey();

   /**
    * Lower key k.
    *
    * @param key the key
    * @return the k
    */
   K lowerKey(K key);

   default Iterator<K> keyIterator(K start, K end) {
      return new Iterator<K>() {
         final Iterator<K> iterator = keyIterator(start);
         boolean hasNext = true;
         K next = null;

         private boolean advance() {
            if (!hasNext) {
               return false;
            }
            while (next == null && iterator.hasNext()) {
               next = iterator.next();
               if (Sorting.compare(next, end) > 0) {
                  next = null;
                  hasNext = false;
                  return false;
               }
            }
            return next != null;
         }

         @Override
         public boolean hasNext() {
            return advance();
         }

         @Override
         public K next() {
            if (!advance()) {
               throw new NoSuchElementException();
            }
            K toReturn = next;
            next = null;
            return toReturn;
         }
      };
   }

   /**
    * Search ceiling iterator.
    *
    * @param startKey          the start key
    * @param traverseCondition the traverse condition
    * @return the iterator
    */
   default Iterator<Entry<K, V>> searchCeiling(K startKey, BiPredicate<K, K> traverseCondition) {
      K ck = ceilingKey(startKey);
      if (ck == null) {
         return Collections.emptyIterator();
      }
      return new Iterator<Entry<K, V>>() {
         private final Iterator<K> backing = keyIterator(ck);
         private K nextKey = null;

         private boolean advance() {
            if (nextKey != null) {
               return true;
            }
            if (backing.hasNext()) {
               nextKey = backing.next();
               if (traverseCondition.test(nextKey, startKey)) {
                  return true;
               }
               nextKey = null;
            }
            return false;
         }

         @Override
         public boolean hasNext() {
            return advance();
         }

         @Override
         public Entry<K, V> next() {
            advance();
            K n = nextKey;
            nextKey = null;
            return $(n, get(n));
         }
      };
   }
}//END OF NavigableKVStore
