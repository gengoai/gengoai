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

package com.gengoai.stream;

import com.gengoai.collection.Sorting;
import com.gengoai.conversion.Cast;
import com.gengoai.function.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

/**
 * <p>A facade for stream classes which contain key-value pairs. Provides a common interface to working with an
 * manipulating streams regardless of their backend implementation. </p>
 *
 * @param <T> the type parameter
 * @param <U> the type parameter
 * @author David B. Bracewell
 */
public interface MPairStream<T, U> extends AutoCloseable {

   /**
    * Caches the stream.
    *
    * @return the cached stream
    */
   MPairStream<T, U> cache();

   /**
    * Collects the items in the stream as a list
    *
    * @return the list of items in the stream
    */
   List<Map.Entry<T, U>> collectAsList();

   /**
    * Collects the items in the stream as a map
    *
    * @return the map of items in the stream
    */
   Map<T, U> collectAsMap();

   /**
    * The number of items in the stream
    *
    * @return the number of items in the stream
    */
   long count();

   /**
    * Filters the stream.
    *
    * @param predicate the predicate to use to determine which objects are kept
    * @return the new stream
    */
   MPairStream<T, U> filter(SerializableBiPredicate<? super T, ? super U> predicate);

   /**
    * Filters the stream by key.
    *
    * @param predicate the predicate to apply to keys in order to determine which objects are kept
    * @return the new stream
    */
   MPairStream<T, U> filterByKey(SerializablePredicate<T> predicate);

   /**
    * Filters the stream by value.
    *
    * @param predicate the predicate to apply to values in order to determine which objects are kept
    * @return the new stream
    */
   MPairStream<T, U> filterByValue(SerializablePredicate<U> predicate);

   /**
    * Maps the key-value pairs to one or more new key-value pairs
    *
    * @param <R>      the new key type parameter
    * @param <V>      the new value type parameter
    * @param function the function to map key-value pairs
    * @return the new pair stream
    */
   <R, V> MPairStream<R, V> flatMapToPair(SerializableBiFunction<? super T, ? super U, Stream<Map.Entry<? extends R, ? extends V>>> function);

   /**
    * Performs an operation on each item in the stream
    *
    * @param consumer the consumer action to perform
    */
   void forEach(SerializableBiConsumer<? super T, ? super U> consumer);

   /**
    * Performs an operation on each item in the stream ensuring that is done locally and not distributed.
    *
    * @param consumer the consumer action to perform
    */
   void forEachLocal(SerializableBiConsumer<? super T, ? super U> consumer);

   /**
    * Gets the context used to create the stream
    *
    * @return the context
    */
   StreamingContext getContext();

   /**
    * Group bys the items in the stream by the key
    *
    * @return the new stream
    */
   MPairStream<T, Iterable<U>> groupByKey();

   default boolean isDistributed() {
      return false;
   }

   MPairStream<T, U> persist(StorageLevel storageLevel);

   /**
    * Determines if the stream is empty or not
    *
    * @return True if empty, False otherwise
    */
   boolean isEmpty();

   /**
    * Can this stream be consumed more the once?
    *
    * @return True the stream can be reused multiple times, False the stream can only be used once
    */
   default boolean isReusable() {
      return false;
   }

   Stream<Map.Entry<T, U>> javaStream();

   /**
    * Performs an inner join between this stream and another using the key to match.
    *
    * @param <V>    the value type parameter of the stream to join with
    * @param stream the other stream to inner join with
    * @return the new stream
    */
   <V> MPairStream<T, Map.Entry<U, V>> join(MPairStream<? extends T, ? extends V> stream);

   /**
    * Returns a stream over the keys in this pair stream
    *
    * @return the key stream
    */
   MStream<T> keys();

   /**
    * Performs a left outer join between this stream and another using the key to match.
    *
    * @param <V>    the value type parameter of the stream to join with
    * @param stream the other stream to left outer join with
    * @return the new stream
    */
   <V> MPairStream<T, Map.Entry<U, V>> leftOuterJoin(MPairStream<? extends T, ? extends V> stream);

   /**
    * Maps the key-value pairs to a new object
    *
    * @param <R>      the type parameter being mapped to
    * @param function the function to map from key-value pairs to objects of type <code>R</code>
    * @return the new stream
    */
   <R> MStream<R> map(SerializableBiFunction<? super T, ? super U, ? extends R> function);

   /**
    * Maps the key-value pairs to doubles
    *
    * @param function the function to map from key-value pairs to double values
    * @return the new double stream
    */
   MDoubleStream mapToDouble(SerializableToDoubleBiFunction<? super T, ? super U> function);

   /**
    * Maps the key-value pairs to new key-value pairs
    *
    * @param <R>      the new key type parameter
    * @param <V>      the new value type parameter
    * @param function the function to map key-value pairs
    * @return the new pair stream
    */
   <R, V> MPairStream<R, V> mapToPair(SerializableBiFunction<? super T, ? super U, ? extends Map.Entry<? extends R, ? extends V>> function);

   /**
    * Returns the max item in the stream using the given comparator to compare items.
    *
    * @param comparator the comparator to use to compare values in the stream
    * @return the optional containing the max value
    */
   Optional<Map.Entry<T, U>> max(SerializableComparator<Map.Entry<T, U>> comparator);

   /**
    * Returns the maximum entry in the stream comparing on key
    *
    * @return the optional containing the entry with max key
    */
   default Optional<Map.Entry<T, U>> maxByKey() {
      return minByKey((t1, t2) -> Sorting.natural().reversed().compare(Cast.as(t1), Cast.as(t2)));
   }

   /**
    * Returns the maximum entry in the stream comparing on key
    *
    * @param comparator the comparator to use to compare keys in the stream
    * @return the optional containing the entry with max key
    */
   default Optional<Map.Entry<T, U>> maxByKey(SerializableComparator<? super T> comparator) {
      return min((t1, t2) -> comparator.compare(t1.getKey(), t2.getKey()));
   }

   /**
    * Returns the maximum entry in the stream comparing on value
    *
    * @return the optional containing the entry with max value
    */
   default Optional<Map.Entry<T, U>> maxByValue() {
      return minByKey((t1, t2) -> Sorting.natural().compare(Cast.as(t1), Cast.as(t2)));
   }

   /**
    * Returns the maximum entry in the stream comparing on value
    *
    * @param comparator the comparator to use to compare value in the stream
    * @return the optional containing the entry with max value
    */
   default Optional<Map.Entry<T, U>> maxByValue(SerializableComparator<? super U> comparator) {
      return min((t1, t2) -> comparator.compare(t1.getValue(), t2.getValue()));
   }

   /**
    * Returns the min item in the stream requiring that the items be comparable.
    *
    * @return the optional containing the min value
    */
   Optional<Map.Entry<T, U>> min(SerializableComparator<Map.Entry<T, U>> comparator);

   /**
    * Returns the minimum entry in the stream comparing on key
    *
    * @return the optional containing the entry with min key
    */
   default Optional<Map.Entry<T, U>> minByKey() {
      return minByKey((t1, t2) -> Sorting.natural().compare(Cast.as(t1), Cast.as(t2)));
   }

   /**
    * Returns the minimum entry in the stream comparing on key
    *
    * @param comparator the comparator to use to compare keys in the stream
    * @return the optional containing the entry with min key
    */
   default Optional<Map.Entry<T, U>> minByKey(SerializableComparator<? super T> comparator) {
      return min((t1, t2) -> comparator.compare(t1.getKey(), t2.getKey()));
   }

   /**
    * Returns the minimum entry in the stream comparing on value
    *
    * @return the optional containing the entry with min value
    */
   default Optional<Map.Entry<T, U>> minByValue() {
      return minByKey((t1, t2) -> Sorting.natural().compare(Cast.as(t1), Cast.as(t2)));
   }

   /**
    * Returns the minimum entry in the stream comparing on value
    *
    * @param comparator the comparator to use to compare value in the stream
    * @return the optional containing the entry with min value
    */
   default Optional<Map.Entry<T, U>> minByValue(SerializableComparator<? super U> comparator) {
      return min((t1, t2) -> comparator.compare(t1.getValue(), t2.getValue()));
   }

   /**
    * Sets the handler to call when the stream is closed. Typically, this is to clean up any open resources, such as
    * file handles.
    *
    * @param closeHandler the handler to run when the stream is closed.
    */
   MPairStream<T,U> onClose(SerializableRunnable closeHandler);

   /**
    * Ensures that the stream is parallel or distributed.
    *
    * @return the new stream
    */
   MPairStream<T, U> parallel();

   /**
    * Performs a reduction by key on the elements of this stream using the given binary operator.
    *
    * @param operator the binary operator used to combine two objects
    * @return the new stream containing keys and reduced values
    */
   MPairStream<T, U> reduceByKey(SerializableBinaryOperator<U> operator);

   /**
    * Repartitions the stream to the given number of partitions. This may be a no-op for some streams, i.e. Local
    * Streams.
    *
    * @param numPartitions the number of partitions the stream should have
    * @return the new stream
    */
   MPairStream<T, U> repartition(int numPartitions);

   /**
    * Performs a right outer join between this stream and another using the key to match.
    *
    * @param <V>    the value type parameter of the stream to join with
    * @param stream the other stream to right outer join with
    * @return the new stream
    */
   <V> MPairStream<T, Map.Entry<U, V>> rightOuterJoin(MPairStream<? extends T, ? extends V> stream);

   /**
    * Randomly samples <code>number</code> items from the stream.
    *
    * @param withReplacement true allow a single item to be represented in the sample multiple times, false allow a
    *                        single item to only be picked once.
    * @param number          the number of items desired in the sample
    * @return the new stream
    */
   MPairStream<T, U> sample(boolean withReplacement, long number);

   /**
    * Shuffles the items in the stream.
    *
    * @return the new stream
    */
   default MPairStream<T, U> shuffle() {
      return shuffle(new Random());
   }

   /**
    * Shuffles the items in the string using the given <code>Random</code> object.
    *
    * @param random the random number generator
    * @return the new stream
    */
   MPairStream<T, U> shuffle(Random random);

   /**
    * Sorts the items in the stream by key in ascending or descending order. Requires items to implement the
    * <code>Comparable</code> interface.
    *
    * @param ascending determines if the items should be sorted in ascending (true) or descending (false) order
    * @return the new stream
    */
   default MPairStream<T, U> sortByKey(boolean ascending) {
      if (ascending) {
         return sortByKey((o1, o2) -> Sorting.natural().compare(Cast.as(o1), Cast.as(o2)));
      }
      return sortByKey((o1, o2) -> Sorting.natural().reversed().compare(Cast.as(o1), Cast.as(o2)));
   }

   /**
    * Sorts the items in the stream by key using the given comparator.
    *
    * @param comparator The comparator to use to comapre keys
    * @return the new stream
    */
   MPairStream<T, U> sortByKey(SerializableComparator<T> comparator);

   /**
    * Unions this stream with another.
    *
    * @param other the other stream to add to this one.
    * @return the new stream
    */
   MPairStream<T, U> union(MPairStream<? extends T, ? extends U> other);

   /**
    * Updates the config instance used for this String
    */
   default void updateConfig() {

   }

   /**
    * Returns a stream of values
    *
    * @return the new stream of values
    */
   MStream<U> values();

}//END OF MPairStream
