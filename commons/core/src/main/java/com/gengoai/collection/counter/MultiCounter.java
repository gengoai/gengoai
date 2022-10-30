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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.collection.Index;
import com.gengoai.collection.Indexes;
import com.gengoai.conversion.Converter;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVWriter;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.JsonEntry;
import com.gengoai.Math2;
import com.gengoai.tuple.Tuple3;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>A specialized object-object to double map that allows basic statistics over the object pairs and their
 * values.</p>
 *
 * @param <K> component type of the first key in the pair
 * @param <V> component type of the second key in the pair
 * @author David B. Bracewell
 */
@JsonDeserialize(as = HashMapMultiCounter.class)
public interface MultiCounter<K, V> {

   /**
    * Constructs a new multi-counter made up of counts that are adjusted using the supplied function.
    *
    * @param function The function to use to adjust the counts
    * @return The new counter with adjusted counts.
    */
   MultiCounter<K, V> adjustValues(DoubleUnaryOperator function);

   /**
    * Adjust the values in-place using the supplied function
    *
    * @param function The function to use to adjust the counts
    * @return this counter
    */
   MultiCounter<K, V> adjustValuesSelf(DoubleUnaryOperator function);

   /**
    * Calculates the average of the values in the counter
    *
    * @return The average count in the counter
    */
   default double average() {
      return Math2.summaryStatistics(values()).getAverage();
   }

   /**
    * Clears the counter
    */
   void clear();

   /**
    * Determines if (item,*) is in the counter, i.e. if the item is a first key in the counter
    *
    * @param item item relating to a first key to check
    * @return True if item is in the counter, false otherwise
    */
   boolean contains(K item);

   /**
    * Determines if the pair (item1,item2) is in the counter.
    *
    * @param item1 the item 1
    * @param item2 the item 2
    * @return the boolean
    */
   boolean contains(K item1, V item2);

   /**
    * Decrements the count of the pair (item1, item2) by one.
    *
    * @param item1 the first key
    * @param item2 the second key
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> decrement(K item1, V item2) {
      return decrement(item1, item2, 1);
   }

   /**
    * Decrements the count of the pair (item1, item2) by the given amount.
    *
    * @param item1  the first key
    * @param item2  the second key
    * @param amount the amount to decrement the (item1,item2) pair by
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> decrement(K item1, V item2, double amount) {
      return increment(item1, item2, -amount);
   }

   /**
    * Decrements the counts of the entries in the iterable in this counter by one.
    *
    * @param iterable the iterable of entries that we want to decrement
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> decrementAll(Iterable<? extends Map.Entry<K, V>> iterable) {
      if(iterable != null) {
         iterable.forEach(e -> decrement(e.getKey(), e.getValue()));
      }
      return this;
   }

   /**
    * Decrements the count of the item and each second key in the iterable by one in this counter.
    *
    * @param item     the first key
    * @param iterable the iterable of second keys
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> decrementAll(K item, Iterable<? extends V> iterable) {
      get(item).decrementAll(iterable);
      return this;
   }

   /**
    * Divides the counter of each first key by its sum.
    *
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> divideByKeySum() {
      firstKeys().forEach(key -> get(key).divideBySum());
      return this;
   }

   /**
    * Divides the values in the counter by the sum and sets the sum to 1.0
    *
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> divideBySum() {
      final double sum = sum();
      adjustValuesSelf(d -> 1d / sum);
      return this;
   }

   /**
    * A set of triplies entries (key1,key2,double) making up the counter
    *
    * @return the set of entries
    */
   @JsonValue
   Set<Tuple3<K, V, Double>> entries();

   /**
    * Creates a new multi-counter containing only those entries whose first key evaluate true for the given predicate
    *
    * @param predicate the predicate to use to filter the first keys
    * @return A new counter containing only those entries whose first key evaluate true for the given predicate
    */
   MultiCounter<K, V> filterByFirstKey(Predicate<K> predicate);

   /**
    * Creates a new multi-counter containing only those entries whose second key evaluate true for the given predicate
    *
    * @param predicate the predicate to use to filter the second keys
    * @return A new counter containing only those entries whose second key evaluate true for the given predicate
    */
   MultiCounter<K, V> filterBySecondKey(Predicate<V> predicate);

   /**
    * Creates a new multi-counter containing only those entries whose value evaluate true for the given predicate
    *
    * @param predicate the predicate to use to filter the values
    * @return A new counter containing only those entries whose value evaluate true for the given predicate
    */
   MultiCounter<K, V> filterByValue(DoublePredicate predicate);

   /**
    * Retrieves a set of the first keys in the counter
    *
    * @return The items making up the first level keys in the counter
    */
   Set<K> firstKeys();

   /**
    * Gets the count of the item1, item2 pair
    *
    * @param item1 the first key
    * @param item2 the second key
    * @return the count of the pair
    */
   default double get(K item1, V item2) {
      if(contains(item1)) {
         return get(item1).get(item2);
      }
      return 0d;
   }

   /**
    * Gets a counter of second keys associated with the first key.
    *
    * @param firstKey the first key whose counter we want
    * @return A counter of second key - double values associated with the first key
    */
   Counter<V> get(K firstKey);

   /**
    * Increments the count of the pair (item1, item2) by one.
    *
    * @param item1 the first key
    * @param item2 the second key
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> increment(K item1, V item2) {
      increment(item1, item2, 1);
      return this;
   }

   /**
    * Increments the count of the pair (item1, item2) by the given amount.
    *
    * @param item1  the first key
    * @param item2  the second key
    * @param amount the amount to increment the (item1,item2) pair by
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> increment(K item1, V item2, double amount) {
      get(item1).increment(item2, amount);
      return this;
   }

   /**
    * Increments the count of the item and each second key in the iterable by one in this counter.
    *
    * @param item     the first key
    * @param iterable the iterable of second keys
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> incrementAll(K item, Iterable<? extends V> iterable) {
      get(item).incrementAll(iterable);
      return this;
   }

   /**
    * Increments the counts of the entries in the iterable in this counter by one.
    *
    * @param iterable the iterable of entries that we want to increments
    * @return This multi-counter (for fluent design)
    */
   default MultiCounter<K, V> incrementAll(Iterable<? extends Map.Entry<K, V>> iterable) {
      if(iterable != null) {
         iterable.forEach(e -> increment(e.getKey(), e.getValue()));
      }
      return this;
   }

   /**
    * Determines if the counter is empty or not
    *
    * @return True if the counter is empty
    */
   @JsonIgnore
   boolean isEmpty();

   /**
    * Returns the items as a sorted list by their counts.
    *
    * @param ascending True if the counts are sorted in ascending order, False if in descending order.
    * @return The sorted list of items.
    */
   List<Map.Entry<K, V>> itemsByCount(boolean ascending);

   /**
    * Retrieves the set of key pairs making up the counts in the counter
    *
    * @return A set of key pairs that make up the items in the counter
    */
   Set<Map.Entry<K, V>> keyPairs();

   /**
    * Calculates the magnitude (square root of sum of squares) of the items in the Counter.
    *
    * @return the magnitude
    */
   default double magnitude() {
      return Math.sqrt(Math2.summaryStatistics(values()).getSumOfSquares());
   }

   /**
    * Determines the maximum value in the counter
    *
    * @return The maximum count in the counter
    */
   default double maximumCount() {
      return Math2.summaryStatistics(values()).getMax();
   }

   /**
    * Merges the counts in one counter with this one.
    *
    * @param other The other counter to merge.
    * @return the multi counter
    */
   MultiCounter<K, V> merge(MultiCounter<K, V> other);

   /**
    * Determines the minimum value in the counter
    *
    * @return The minimum count in the counter
    */
   default double minimumCount() {
      return Math2.summaryStatistics(values()).getMin();
   }

   /**
    * Removes an item and its associated secondary keys from the counter
    *
    * @param item The first level key to remove
    * @return the counter associated with the removed item
    */
   Counter<V> remove(K item);

   /**
    * Removes a key pair from the counter.
    *
    * @param item1 the first key
    * @param item2 the second key
    * @return the count of the key pair
    */
   double remove(K item1, V item2);

   /**
    * Removes all the given items from the counter
    *
    * @param items The items to remove
    * @return the multi counter
    */
   default MultiCounter<K, V> removeAll(Iterable<K> items) {
      if(items != null) {
         items.forEach(this::remove);
      }
      return this;
   }

   /**
    * Retrieves an unmodifiable set of secondary level keys in the counter
    *
    * @return an unmodifiable set of secondary level keys in the counter
    */
   default Set<V> secondKeys() {
      return entries().parallelStream().map(Tuple3::getV2).collect(Collectors.toSet());
   }

   /**
    * Sets the value of the given key pair to the given amount
    *
    * @param item1  the first key
    * @param item2  the second key
    * @param amount the amount to set the key pair to
    * @return This MultiCounter (for fluent design)
    */
   MultiCounter<K, V> set(K item1, V item2, double amount);

   /**
    * Sets the secondary keys and counts associated with a first level kek
    *
    * @param item    the first key
    * @param counter the counter of secondary keys and counts
    * @return This MultiCounter (for fluent design)
    */
   MultiCounter<K, V> set(K item, Counter<V> counter);

   default double setIfAbsent(K key1, V key2, BiFunction<K, V, Double> function) {
      return get(key1).asMap().computeIfAbsent(key2, v -> function.apply(key1, v));
   }

   /**
    * The total number of items in the counter
    *
    * @return The number of items in the counter
    */
   int size();

   /**
    * Calculates the standard deviation of the items in the counter
    *
    * @return The standard deviation of the counts in the counter
    */
   default double standardDeviation() {
      return Math2.summaryStatistics(values()).getSampleStandardDeviation();
   }

   /**
    * The sum of values in the counter
    *
    * @return The sum of the counts in the counter
    */
   default double sum() {
      return Math2.summaryStatistics(values()).getSum();
   }

   default JsonEntry toJson() {
      JsonEntry entry = JsonEntry.object();
      final Index<K> firstKeys = Indexes.indexOf(firstKeys());
      entry.addProperty("firstKeys", firstKeys.asList());
      final Index<V> secondKeys = Indexes.indexOf(secondKeys());
      entry.addProperty("secondKeys", secondKeys.asList());
      JsonEntry values = JsonEntry.array();
      for(int i = 0; i < firstKeys.size(); i++) {
         JsonEntry value = JsonEntry.object();
         get(firstKeys.get(i)).forEach((k, v) -> value.addProperty(Integer.toString(secondKeys.getId(k)), v));
         values.addValue(value);
      }
      entry.addProperty("values", values);
      return entry;
   }

   /**
    * Transpose multi counter.
    *
    * @return the multi counter
    */
   default MultiCounter<V, K> transpose() {
      MultiCounter<V, K> mc = MultiCounters.newMultiCounter();
      entries().forEach(e -> mc.set(e.v2, e.v1, e.v3));
      return mc;
   }

   /**
    * The values associated with the items in the counter
    *
    * @return The values of the items in the counter.
    */
   Collection<Double> values();

   /**
    * Writes the counter items and values to CSV
    *
    * @param output the resource to write to
    * @throws IOException Something went wrong writing
    */
   default void writeCsv(Resource output) throws IOException {
      DecimalFormat decimalFormat = new DecimalFormat("#.#####");
      try(CSVWriter writer = CSV.builder().writer(output)) {
         for(Tuple3<K, V, Double> entry : entries()) {
            writer.write(Converter.convertSilently(entry.v1, String.class),
                         Converter.convertSilently(entry.v2, String.class),
                         decimalFormat.format(entry.v3)
                        );
         }
      }
   }

}//END OF MultiCounter
