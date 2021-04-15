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

import com.gengoai.function.*;
import com.gengoai.math.EnhancedDoubleStatistics;

import java.util.OptionalDouble;
import java.util.PrimitiveIterator;

/**
 * <p>A facade for double stream classes, such as Java's <code>DoubleStream</code> and Spark's <code>DoubleRDD</code>
 * objects. Provides a common interface to working with an manipulating streams regardless of their backend
 * implementation. </p>
 *
 * @author David B. Bracewell
 */
public interface MDoubleStream extends AutoCloseable {

   /**
    * Determines if all doubles in the stream evaluate to true with the given predicate
    *
    * @param predicate the predicate to test double entries with
    * @return True if all elements in the stream evaluate to true
    */
   boolean allMatch(SerializableDoublePredicate predicate);

   /**
    * Determines if any of the doubles in the stream evaluate to true with the given predicate
    *
    * @param predicate the predicate to test double entries with
    * @return True if any of the elements in the stream evaluate to true
    */
   boolean anyMatch(SerializableDoublePredicate predicate);

   /**
    * Caches the stream.
    *
    * @return the cached stream
    */
   MDoubleStream cache();

   /**
    * The number of items in the stream
    *
    * @return the number of items in the stream
    */
   long count();

   /**
    * Removes duplicates from the stream
    *
    * @return the new stream without duplicates
    */
   MDoubleStream distinct();

   /**
    * Filters the stream.
    *
    * @param predicate the predicate to use to determine which objects are kept
    * @return the new stream
    */
   MDoubleStream filter(SerializableDoublePredicate predicate);

   /**
    * Gets the first item in the stream
    *
    * @return the optional containing the first item
    */
   OptionalDouble first();

   /**
    * Maps the doubles in this stream to one or more new doubles using the given function.
    *
    * @param mapper the function to use to map objects
    * @return the new stream
    */
   MDoubleStream flatMap(SerializableDoubleFunction<double[]> mapper);

   /**
    * Performs an operation on each item in the stream
    *
    * @param consumer the consumer action to perform
    */
   void forEach(SerializableDoubleConsumer consumer);

   /**
    * Gets the context used to create the stream
    *
    * @return the context
    */
   StreamingContext getContext();

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
   PrimitiveIterator.OfDouble iterator();

   /**
    * Limits the stream to the first <code>number</code> items.
    *
    * @param n the number of items desired
    * @return the new stream of size <code>number</code>
    */
   MDoubleStream limit(int n);

   /**
    * Maps the doubles in the stream using the given function
    *
    * @param mapper the function to use to map doubles
    * @return the new stream
    */
   MDoubleStream map(SerializableDoubleUnaryOperator mapper);

   /**
    * Maps the doubles in the stream to objects using the given function
    *
    * @param <T>      the component type of the returning stream
    * @param function the function to use to map doubles
    * @return the new stream
    */
   <T> MStream<T> mapToObj(SerializableDoubleFunction<? extends T> function);

   /**
    * Returns the max item in the stream.
    *
    * @return the optional containing the max value
    */
   OptionalDouble max();

   /**
    * The mean value of the stream
    *
    * @return the mean value
    */
   double mean();

   /**
    * Returns the min item in the stream.
    *
    * @return the optional containing the min value
    */
   OptionalDouble min();

   /**
    * Determines if none of the doubles in the stream evaluate to true with the given predicate
    *
    * @param predicate the predicate to test double entries with
    * @return True if none of the elements in the stream evaluate to true
    */
   boolean noneMatch(SerializableDoublePredicate predicate);

   /**
    * Sets the handler to call when the stream is closed. Typically, this is to clean up any open resources, such as
    * file handles.
    *
    * @param onCloseHandler the handler to run when the stream is closed.
    */
   MDoubleStream onClose(SerializableRunnable onCloseHandler);

   boolean isDistributed();

   /**
    * Ensures that the stream is parallel or distributed.
    *
    * @return the new stream
    */
   MDoubleStream parallel();

   /**
    * Performs a reduction on the elements of this stream using the given binary operator.
    *
    * @param operator the binary operator used to combine two objects
    * @return the optional describing the reduction
    */
   OptionalDouble reduce(SerializableDoubleBinaryOperator operator);

   /**
    * Performs a reduction on the elements of this stream using the given binary operator.
    *
    * @param zeroValue the starting value for the reduction
    * @param operator  the binary operator used to combine two objects
    * @return the optional describing the reduction
    */
   double reduce(double zeroValue, SerializableDoubleBinaryOperator operator);

   /**
    * Repartitions the stream to the given number of partitions. This may be a no-op for some streams, i.e. Local
    * Streams.
    *
    * @param numberOfPartition the number of partitions the stream should have
    * @return the new stream
    */
   MDoubleStream repartition(int numberOfPartition);

   /**
    * Skips the first <code>n</code> items in the stream
    *
    * @param n the number of items in the stream
    * @return the new stream
    */
   MDoubleStream skip(int n);

   /**
    * Sorts the double stream in ascending order.
    *
    * @param ascending determines if the items should be sorted in ascending (true) or descending (false) order
    * @return the new double stream
    */
   MDoubleStream sorted(boolean ascending);

   /**
    * Calculates a number of useful statistics over the elements in the stream
    *
    * @return the statistics
    */
   EnhancedDoubleStatistics statistics();

   /**
    * Calculates the standard deviation of the stream
    *
    * @return the standard deviation
    */
   double stddev();

   /**
    * Calculates the sum of the stream
    *
    * @return the sum
    */
   double sum();

   /**
    * Collects the values of the stream as a double array.
    *
    * @return the double array
    */
   double[] toArray();

   /**
    * Unions this stream with another
    *
    * @param other the other stream to append to this one
    * @return the new double stream
    */
   MDoubleStream union(MDoubleStream other);


   /**
    * Updates the config instance used for this String
    */
   default void updateConfig() {

   }

}//END OF MDoubleStream
