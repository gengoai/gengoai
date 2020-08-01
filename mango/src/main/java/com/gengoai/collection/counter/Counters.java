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
import com.gengoai.io.CSV;
import com.gengoai.io.CSVReader;
import com.gengoai.io.resource.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Common methods for reading counters from structured files, creating synchronized and unmodifiable wrappers.
 */
public final class Counters {

   private Counters() {
      throw new IllegalAccessError();
   }


   /**
    * <p>Creates a new {@link HashMapMultiCounter} which is initialized with the given items</p>
    *
    * @param <T>   the component type of the counter
    * @param items the items to add to the counter
    * @return the counter
    */
   @SafeVarargs
   public static <T> Counter<T> newCounter(T... items) {
      Counter<T> counter = new HashMapCounter<>();
      if (items != null) {
         counter.incrementAll(Arrays.asList(items));
      }
      return counter;
   }

   /**
    * <p>Creates a new {@link HashMapMultiCounter} which is initialized with the given items</p>
    *
    * @param <T>      the component type of the counter
    * @param iterable the items to add to the counter
    * @return the counter
    */
   public static <T> Counter<T> newCounter(Iterable<? extends T> iterable) {
      Counter<T> counter = new HashMapCounter<>();
      counter.incrementAll(iterable);
      return counter;
   }

   /**
    * <p>Creates a new {@link HashMapMultiCounter} which is initialized with the given items</p>
    *
    * @param <T>    the component type of the counter
    * @param stream the items to add to the counter
    * @return the counter
    */
   public static <T> Counter<T> newCounter(Stream<? extends T> stream) {
      Counter<T> counter = new HashMapCounter<>();
      stream.forEach(counter::increment);
      return counter;
   }

   /**
    * <p>Creates a new {@link HashMapMultiCounter} which is initialized by merging with the given map</p>
    *
    * @param <T> the component type of the counter
    * @param map the items and counts to merge with counter
    * @return the counter
    */
   public static <T> Counter<T> newCounter(Map<? extends T, ? extends Number> map) {
      Counter<T> counter = new HashMapCounter<>();
      counter.merge(map);
      return counter;
   }

   /**
    * <p>Creates a new {@link HashMapMultiCounter} which is initialized by merging with the given counter</p>
    *
    * @param <T>   the component type of the counter
    * @param other the items and counts to merge with counter
    * @return the counter
    */
   public static <T> Counter<T> newCounter(Counter<? extends T> other) {
      Counter<T> counter = new HashMapCounter<>();
      counter.merge(other);
      return counter;
   }

   /**
    * <p>Reads a counter from a CSV file.</p>
    *
    * @param <TYPE>   the component type of the counter
    * @param resource the resource that the counter values are read from.
    * @param keyClass the class of the item type
    * @return the counter
    * @throws IOException Something went wrong reading in the counter.
    */
   public static <TYPE> Counter<TYPE> readCsv(Resource resource, Class<TYPE> keyClass) throws IOException {
      Counter<TYPE> counter = Counters.newCounter();
      try (CSVReader reader = CSV.builder().reader(resource)) {
         reader.forEach(row -> {
            if (row.size() >= 2) {
               counter.increment(Converter.convertSilently(row.get(0), keyClass), Double.valueOf(row.get(1)));
            }
         });
      }
      return counter;
   }

   /**
    * <p>Wraps a counter making each method call synchronized.</p>
    *
    * @param <TYPE>  the item type
    * @param counter the counter to wrap
    * @return the wrapped counter
    */
   public static <TYPE> Counter<TYPE> newConcurrentCounter(Counter<TYPE> counter) {
      return new ConcurrentHashMapCounter<TYPE>().merge(counter);
   }

   /**
    * <p>Creates a new {@link ConcurrentHashMapCounter} which is initialized with the given items</p>
    *
    * @param <T>   the component type of the counter
    * @param items the items to add to the counter
    * @return the counter
    */
   @SafeVarargs
   public static <T> Counter<T> newConcurrentCounter(T... items) {
      Counter<T> counter = new ConcurrentHashMapCounter<>();
      if (items != null) {
         counter.incrementAll(Arrays.asList(items));
      }
      return counter;
   }


   /**
    * <p>Creates a new {@link ConcurrentHashMapCounter} which is initialized by merging with the given map</p>
    *
    * @param <T> the component type of the counter
    * @param map the items and counts to merge with counter
    * @return the counter
    */
   public static <T> Counter<T> newConcurrentCounter(Map<? extends T, ? extends Number> map) {
      Counter<T> counter = new ConcurrentHashMapCounter<>();
      counter.merge(map);
      return counter;
   }

   /**
    * <p>Creates a new {@link ConcurrentHashMapCounter} which is initialized with the given items</p>
    *
    * @param <T>      the component type of the counter
    * @param iterable the items to add to the counter
    * @return the counter
    */
   public static <T> Counter<T> newConcurrentCounter(Iterable<? extends T> iterable) {
      Counter<T> counter = new ConcurrentHashMapCounter<>();
      counter.incrementAll(iterable);
      return counter;
   }

   /**
    * <p>Creates a new {@link ConcurrentHashMapCounter} which is initialized with the given items</p>
    *
    * @param <T>    the component type of the counter
    * @param stream the items to add to the counter
    * @return the counter
    */
   public static <T> Counter<T> newConcurrentCounter(Stream<? extends T> stream) {
      Counter<T> counter = new ConcurrentHashMapCounter<>();
      stream.forEach(counter::increment);
      return counter;
   }


}//END OF Counters
