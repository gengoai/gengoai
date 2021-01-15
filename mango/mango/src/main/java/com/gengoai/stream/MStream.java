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
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.stream.spark.SparkStream;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * <p>A facade for stream classes, such as Java's <code>Stream</code> and Spark's <code>RDD</code> objects. Provides a
 * common interface to working with an manipulating streams regardless of their backend implementation. </p>
 *
 * @param <T> the component type of the stream
 * @author David B. Bracewell
 */
public interface MStream<T> extends AutoCloseable, Iterable<T> {


   /**
    * Caches the stream.
    *
    * @return the cached stream
    */
   MStream<T> cache();

   /**
    * Performs a reduction on the string using hte given collector.
    *
    * @param <R>       the component type of the collection after applying the collector
    * @param collector the collector to use in reducing the stream
    * @return the result of the collector
    */
   <R> R collect(Collector<? super T, ?, R> collector);

   /**
    * Collects the items in the stream as a list
    *
    * @return the list of items in the stream
    */
   List<T> collect();

   /**
    * The number of items in the stream
    *
    * @return the number of items in the stream
    */
   long count();

   /**
    * Counts the number of times each item occurs in the stream
    *
    * @return a map of object - long counts
    */
   Map<T, Long> countByValue();

   /**
    * Removes duplicates from the stream
    *
    * @return the new stream without duplicates
    */
   MStream<T> distinct();

   /**
    * Filters the stream.
    *
    * @param predicate the predicate to use to determine which objects are kept
    * @return the new stream
    */
   MStream<T> filter(SerializablePredicate<? super T> predicate);

   /**
    * Gets the first item in the stream
    *
    * @return the optional containing the first item
    */
   Optional<T> first();

   /**
    * Maps the objects in this stream to one or more new objects using the given function.
    *
    * @param <R>    the component type of the returning stream
    * @param mapper the function to use to map objects
    * @return the new stream
    */
   <R> MStream<R> flatMap(SerializableFunction<? super T, Stream<? extends R>> mapper);

   /**
    * Maps the objects in this stream to one or more new key-value pairs using the given function.
    *
    * @param <R>      the key type parameter
    * @param <U>      the value type parameter
    * @param function the function to use to map objects
    * @return the new pair stream
    */
   <R, U> MPairStream<R, U> flatMapToPair(SerializableFunction<? super T, Stream<? extends Map.Entry<? extends R, ? extends U>>> function);

   /**
    * Performs a reduction on the elements of this stream using the given binary operator.
    *
    * @param zeroValue The initial value
    * @param operator  the binary operator used to combine two objects
    * @return the optional describing the reduction
    */
   T fold(T zeroValue, SerializableBinaryOperator<T> operator);

   /**
    * Performs an operation on each item in the stream
    *
    * @param consumer the consumer action to perform
    */
   void forEach(SerializableConsumer<? super T> consumer);

   /**
    * Performs an operation on each item in the stream ensuring that is done locally and not distributed.
    *
    * @param consumer the consumer action to perform
    */
   void forEachLocal(SerializableConsumer<? super T> consumer);

   /**
    * Gets the context used to create the stream
    *
    * @return the context
    */
   StreamingContext getContext();

   /**
    * Groups the items in the stream using the given function that maps objects to key values
    *
    * @param <U>      the key type parameter
    * @param function the function that determines the key of the objects in the stream
    * @return the new pair stream
    */
   <U> MPairStream<U, Iterable<T>> groupBy(SerializableFunction<? super T, ? extends U> function);

   /**
    * Returns a new MStream containing the intersection of elements in this stream and the argument stream.
    *
    * @param other Stream to perform intersection with
    * @return the new stream
    */
   MStream<T> intersection(MStream<T> other);

   /**
    * Is distributed boolean.
    *
    * @return True if the stream is distributed
    */
   boolean isDistributed();

   /**
    * Determines if the stream is empty or not
    *
    * @return True if empty, False otherwise
    */
   boolean isEmpty();


   /**
    * Gets an iterator for the stream
    *
    * @return the iterator of items in the stream
    */
   Iterator<T> iterator();

   /**
    * Converts this stream into a java stream
    *
    * @return the java stream
    */
   Stream<T> javaStream();

   /**
    * Limits the stream to the first <code>number</code> items.
    *
    * @param number the number of items desired
    * @return the new stream of size <code>number</code>
    */
   MStream<T> limit(long number);

   /**
    * Maps the objects in the stream using the given function
    *
    * @param <R>      the component type of the returning stream
    * @param function the function to use to map objects
    * @return the new stream
    */
   <R> MStream<R> map(SerializableFunction<? super T, ? extends R> function);

   /**
    * Maps objects in this stream to double values
    *
    * @param function the function to convert objects to doubles
    * @return the new double stream
    */
   MDoubleStream mapToDouble(SerializableToDoubleFunction<? super T> function);

   /**
    * Maps the objects in this stream to a key-value pair using the given function.
    *
    * @param <R>      the key type parameter
    * @param <U>      the value type parameter
    * @param function the function to use to map objects
    * @return the new pair stream
    */
   <R, U> MPairStream<R, U> mapToPair(SerializableFunction<? super T, ? extends Map.Entry<? extends R, ? extends U>> function);

   /**
    * Returns the max item in the stream requiring that the items be comparable.
    *
    * @return the optional containing the max value
    */
   default Optional<T> max() {
      return min((t1, t2) -> Sorting.natural().reversed().compare(Cast.as(t1), Cast.as(t2)));
   }

   /**
    * Returns the max item in the stream using the given comparator to compare items.
    *
    * @param comparator the comparator to use to compare values in the stream
    * @return the optional containing the max value
    */
   Optional<T> max(SerializableComparator<? super T> comparator);

   /**
    * Returns the min item in the stream requiring that the items be comparable.
    *
    * @return the optional containing the min value
    */
   default Optional<T> min() {
      return min((t1, t2) -> Sorting.natural().compare(Cast.as(t1), Cast.as(t2)));
   }

   /**
    * Returns the min item in the stream using the given comparator to compare items.
    *
    * @param comparator the comparator to use to compare values in the stream
    * @return the optional containing the min value
    */
   Optional<T> min(SerializableComparator<? super T> comparator);

   /**
    * Sets the handler to call when the stream is closed. Typically, this is to clean up any open resources, such as
    * file handles.
    *
    * @param closeHandler the handler to run when the stream is closed.
    * @return the m stream
    */
   MStream<T> onClose(SerializableRunnable closeHandler);

   /**
    * Ensures that the stream is parallel or distributed.
    *
    * @return the new stream
    */
   MStream<T> parallel();

   /**
    * Partitions the stream into iterables each of size {@code <=}  <code>partitionSize</code>.
    *
    * @param partitionSize the desired number of objects in each partition
    * @return the new stream
    */
   MStream<Stream<T>> partition(long partitionSize);

   /**
    * Persists the stream to the given storage level
    *
    * @param storageLevel the storage level
    * @return the persisted MStream
    */
   MStream<T> persist(StorageLevel storageLevel);

   /**
    * Performs a reduction on the elements of this stream using the given binary operator.
    *
    * @param reducer the binary operator used to combine two objects
    * @return the optional describing the reduction
    */
   Optional<T> reduce(SerializableBinaryOperator<T> reducer);

   /**
    * Repartitions the stream to the given number of partitions. This may be a no-op for some streams, i.e. Local
    * Streams.
    *
    * @param numPartitions the number of partitions the stream should have
    * @return the new stream
    */
   MStream<T> repartition(int numPartitions);

   /**
    * Randomly samples <code>number</code> items from the stream.
    *
    * @param withReplacement true allow a single item to be represented in the sample multiple times, false allow a
    *                        single item to only be picked once.
    * @param number          the number of items desired in the sample
    * @return the new stream
    */
   MStream<T> sample(boolean withReplacement, int number);

   /**
    * Save as the stream to a text file at the given location. Writing may result in multiple files being created.
    *
    * @param location the location to write the stream to
    */
   void saveAsTextFile(Resource location);

   /**
    * Save as the stream to a text file at the given location. Writing may result in multiple files being created.
    *
    * @param location the location to write the stream to
    */
   default void saveAsTextFile(String location) {
      saveAsTextFile(Resources.from(location));
   }

   /**
    * Shuffles the items in the stream.
    *
    * @return the new stream
    */
   default MStream<T> shuffle() {
      return shuffle(new Random(0));
   }

   /**
    * Shuffles the items in the string using the given <code>Random</code> object.
    *
    * @param random the random number generator
    * @return the new stream
    */
   MStream<T> shuffle(Random random);

   /**
    * Skips the first <code>n</code> items in the stream
    *
    * @param n the number of items in the stream
    * @return the new stream
    */
   MStream<T> skip(long n);

   /**
    * Sorts the items in the stream in ascending or descending order using the given keyFunction to determine how to
    * compare.
    *
    * @param <R>         the type parameter
    * @param ascending   determines if the items should be sorted in ascending (true) or descending (false) order
    * @param keyFunction function to use to convert the items in the stream to something that is comparable.
    * @return the new stream
    */
   <R extends Comparable<R>> MStream<T> sortBy(boolean ascending,
                                               SerializableFunction<? super T, ? extends R> keyFunction);

   /**
    * Sorts the items in the stream in ascending or descending order. Requires items to implement the
    * <code>Comparable</code> interface.
    *
    * @param ascending determines if the items should be sorted in ascending (true) or descending (false) order
    * @return the new stream
    */
   default MStream<T> sorted(boolean ascending) {
      return sortBy(ascending, Cast::as);
   }

   /**
    * Takes the first <code>n</code> items from the stream.
    *
    * @param n the number of items to take
    * @return a list of the first n items
    */
   List<T> take(int n);


   /**
    * To distributed stream spark stream.
    *
    * @return A distributed version of the stream
    */
   default SparkStream<T> toDistributedStream() {
      return new SparkStream<>(this);
   }

   /**
    * Unions this stream with another.
    *
    * @param other the other stream to add to this one.
    * @return the new stream
    */
   MStream<T> union(MStream<T> other);

   /**
    * Updates the config instance used for this stream
    */
   default void updateConfig() {

   }

   /**
    * <p>Zips (combines) this stream together with the given other creating a pair stream. For example, if this stream
    * contains [1,2,3] and stream 2 contains [4,5,6] the result would be a pair stream containing the key value pairs
    * [(1,4), (2,5), (3,6)]. Note that the length of the resulting stream will be the minimum of the two streams.</p>
    *
    * @param <U>   the component type of the second stream
    * @param other the stream making up the value in the resulting entries
    * @return a new pair stream with keys from this stream and values for the other stream
    */
   <U> MPairStream<T, U> zip(MStream<U> other);

   /**
    * Creates a pair stream where the keys are items in this stream and values are the index (starting at 0) of the item
    * in the stream.
    *
    * @return the new pair stream
    */
   MPairStream<T, Long> zipWithIndex();

}//END OF MStream
