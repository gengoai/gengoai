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

package com.gengoai.collection.counter;

import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVReader;
import com.gengoai.io.resource.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Common methods for reading multi-counters from structured files, creating synchronized and unmodifiable wrappers.
 */
public final class MultiCounters {


   private MultiCounters() {
      throw new IllegalAccessError();
   }


   /**
    * Creates a copy of the specified multi-counter
    *
    * @param <K1>    the component type of the first key
    * @param <K2>    the component type of the second key
    * @param counter the counter to copy
    * @return A new MultiCounter that is a copy of the given MultiCounter
    */
   public static <K1, K2> MultiCounter<K1, K2> newMultiCounter(MultiCounter<? extends K1, ? extends K2> counter) {
      MultiCounter<K1, K2> mc = new HashMapMultiCounter<>();
      counter.entries().forEach(triple -> mc.increment(triple.v1, triple.v2, triple.v3));
      return mc;
   }

   /**
    * Creates a new MultiCounter using the given map entries.
    *
    * @param <K1>    the component type of the first key
    * @param <K2>    the component type of the second key
    * @param entries the entries to increment in the counter.
    * @return A new MultiCounter with counts of the given entries
    */
   @SafeVarargs
   public static <K1, K2> MultiCounter<K1, K2> newMultiCounter(Map.Entry<? extends K1, ? extends K2>... entries) {
      return entries == null ? new HashMapMultiCounter<>() : newMultiCounter(Arrays.asList(entries));
   }

   /**
    * Creates a new MultiCounter using the given map entries.
    *
    * @param <K1>    the component type of the first key
    * @param <K2>    the component type of the second key
    * @param entries the entries to increment in the counter.
    * @return A new MultiCounter with counts of the given entries
    */
   public static <K1, K2> MultiCounter<K1, K2> newMultiCounter(Iterable<? extends Map.Entry<? extends K1, ? extends K2>> entries) {
      MultiCounter<K1, K2> mc = new HashMapMultiCounter<>();
      entries.forEach(e -> mc.increment(e.getKey(), e.getValue()));
      return mc;
   }

   /**
    * Creates a new MultiCounter using the given map.
    *
    * @param <K1> the component type of the first key
    * @param <K2> the component type of the second key
    * @param map  A map whose keys are the entries of the counter and values are the counts.
    * @return A new MultiCounter with counts of the given entries
    */
   public static <K1, K2> MultiCounter<K1, K2> newMultiCounter(Map<? extends Map.Entry<? extends K1, ? extends K2>, ? extends Number> map) {
      MultiCounter<K1, K2> mc = new HashMapMultiCounter<>();
      map.forEach((key, value) -> mc.increment(key.getKey(), key.getValue(), value.doubleValue()));
      return mc;
   }

   /**
    * <p>Creates a new ConcurrentHashMapMultiCounter using the given multi counter.</p>
    *
    * @param <K1>         the component type of the first key
    * @param <K2>         the component type of the second key
    * @param multiCounter the multi counter to wrap
    * @return the synchronized multi-counter
    */
   public static <K1, K2> MultiCounter<K1, K2> newConcurrentMultiCounter(MultiCounter<K1, K2> multiCounter) {
      return new ConcurrentHashMapMultiCounter<K1, K2>().merge(multiCounter);
   }


   /**
    * Creates a new ConcurrentHashMapMultiCounter using the given map entries.
    *
    * @param <K1>    the component type of the first key
    * @param <K2>    the component type of the second key
    * @param entries the entries to increment in the counter.
    * @return A new MultiCounter with counts of the given entries
    */
   @SafeVarargs
   public static <K1, K2> MultiCounter<K1, K2> newConcurrentMultiCounter(Map.Entry<? extends K1, ? extends K2>... entries) {
      return entries == null ? new ConcurrentHashMapMultiCounter<>() : newConcurrentMultiCounter(
         Arrays.asList(entries));
   }

   /**
    * Creates a new ConcurrentHashMapMultiCounter using the given map entries.
    *
    * @param <K1>    the component type of the first key
    * @param <K2>    the component type of the second key
    * @param entries the entries to increment in the counter.
    * @return A new MultiCounter with counts of the given entries
    */
   public static <K1, K2> MultiCounter<K1, K2> newConcurrentMultiCounter(Iterable<? extends Map.Entry<? extends K1, ? extends K2>> entries) {
      MultiCounter<K1, K2> mc = new ConcurrentHashMapMultiCounter<>();
      entries.forEach(e -> mc.increment(e.getKey(), e.getValue()));
      return mc;
   }

   /**
    * Creates a new ConcurrentHashMapMultiCounter using the given map.
    *
    * @param <K1> the component type of the first key
    * @param <K2> the component type of the second key
    * @param map  A map whose keys are the entries of the counter and values are the counts.
    * @return A new MultiCounter with counts of the given entries
    */
   public static <K1, K2> MultiCounter<K1, K2> newConcurrentMultiCounter(Map<? extends Map.Entry<? extends K1, ? extends K2>, ? extends Number> map) {
      MultiCounter<K1, K2> mc = new ConcurrentHashMapMultiCounter<>();
      map.forEach((key, value) -> mc.increment(key.getKey(), key.getValue(), value.doubleValue()));
      return mc;
   }

   /**
    * <p>Reads a multi-counter from a CSV file.</p>
    *
    * @param <K1>      the component type of the first key
    * @param <K2>      the component type of the second key
    * @param resource  the resource that the counter values are read from.
    * @param key1Class the class of first key
    * @param key2Class the class of the second key
    * @return the new MultiCounter
    * @throws IOException Something went wrong reading in the counter.
    */
   public static <K1, K2> MultiCounter<K1, K2> readCsv(Resource resource, Class<K1> key1Class, Class<K2> key2Class) throws IOException {
      MultiCounter<K1, K2> counter = newMultiCounter();
      try (CSVReader reader = CSV.builder().reader(resource)) {
         for (List<String> row : reader) {
            if (row.size() >= 3) {
               try {
                  counter.increment(Converter.convert(row.get(0), key1Class),
                                    Converter.convert(row.get(1), key2Class),
                                    Double.valueOf(row.get(2)));
               } catch (TypeConversionException e) {
                  throw new IOException(e);
               }
            }
         }
      }
      return counter;
   }

}//END OF MultiCounters
