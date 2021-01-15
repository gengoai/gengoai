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

package com.gengoai.concurrent;

import com.gengoai.SystemInfo;
import com.gengoai.collection.Iterables;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Convenience methods for manipulating threads and processing data on multiple-threads
 *
 * @author David B. Bracewell
 */
public final class Threads {

   private Threads() {
      throw new IllegalAccessError();
   }

   /**
    * <p> Sleeps the thread suppressing any errors. </p>
    *
    * @param milliseconds The amount of time in milliseconds to sleep
    */
   public static void sleep(long milliseconds) {
      if (milliseconds <= 0) {
         return;
      }
      try {
         Thread.sleep(milliseconds);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   /**
    * <p> Sleeps the thread suppressing any errors for a given time unit. </p>
    *
    * @param time     The amount of time to sleep
    * @param timeUnit The TimeUnit that the time is in
    */
   public static void sleep(long time, TimeUnit timeUnit) {
      sleep(timeUnit.toMillis(time));
   }


   /**
    * Processes the given iterator across the given <code>SystemInfo.NUMBER_OF_PROCESSORS</code> threads  using a buffer
    * of size <code>SystemInfo.NUMBER_OF_PROCESSORS * 100</code> to feed the given consumer.
    *
    * @param <T>      the type of element being processed
    * @param iterator the items being processed
    * @param consumer the consumer to process the items in the iterator
    * @return True if the operation completed successfully
    */
   public static <T> boolean process(Iterator<T> iterator, Consumer<T> consumer) {
      return process(Iterables.asIterable(iterator), consumer);
   }

   /**
    * Processes the given iterator across the given <code>numberOfConsumerThreads</code> using a buffer of given size to
    * feed the given consumer.
    *
    * @param <T>                     the type of element being processed
    * @param iterator                the items being processed
    * @param consumer                the consumer to process the items in the iterator
    * @param numberOfConsumerThreads the number of consumers to run
    * @param bufferSize              the size of the buffer used for feeding the consumer threads
    * @return True if the operation completed successfully
    */
   public static <T> boolean process(Iterator<T> iterator,
                                     Consumer<T> consumer,
                                     int numberOfConsumerThreads,
                                     int bufferSize) {
      return process(Iterables.asIterable(iterator), consumer, numberOfConsumerThreads, bufferSize);
   }

   /**
    * Processes the given iterable across the given <code>SystemInfo.NUMBER_OF_PROCESSORS</code> threads  using a buffer
    * of size <code>SystemInfo.NUMBER_OF_PROCESSORS * 100</code> to feed the given consumer.
    *
    * @param <T>      the type of element being processed
    * @param iterable the items being processed
    * @param consumer the consumer to process the items in the iterable
    * @return True if the operation completed successfully
    */
   public static <T> boolean process(Iterable<T> iterable, Consumer<T> consumer) {
      return process(iterable, consumer, SystemInfo.NUMBER_OF_PROCESSORS, SystemInfo.NUMBER_OF_PROCESSORS * 100);
   }

   /**
    * Processes the given iterable across the given <code>numberOfConsumerThreads</code> using a buffer of given size to
    * feed the given consumer.
    *
    * @param <T>                     the type of element being processed
    * @param iterable                the items being processed
    * @param consumer                the consumer to process the items in the iterable
    * @param numberOfConsumerThreads the number of consumers to run
    * @param bufferSize              the size of the buffer used for feeding the consumer threads
    * @return True if the operation completed successfully
    */
   public static <T> boolean process(Iterable<T> iterable,
                                     Consumer<T> consumer,
                                     int numberOfConsumerThreads,
                                     int bufferSize) {
      return Broker.<T>builder()
         .addProducer(new IterableProducer<>(iterable))
         .addConsumer(consumer, numberOfConsumerThreads)
         .bufferSize(bufferSize)
         .build()
         .run();
   }

   /**
    * Processes the given iterable across the given <code>numberOfConsumerThreads</code> using a buffer of size
    * <code>numberOfConsumerThreads * 100</code> to feed the given consumer.
    *
    * @param <T>                     the type of element being processed
    * @param iterable                the items being processed
    * @param consumer                the consumer to process the items in the iterable
    * @param numberOfConsumerThreads the number of consumers to run
    * @return True if the operation completed successfully
    */
   public static <T> boolean process(Iterable<T> iterable, Consumer<T> consumer, int numberOfConsumerThreads) {
      return Broker.<T>builder()
         .addProducer(new IterableProducer<>(iterable))
         .addConsumer(consumer, numberOfConsumerThreads)
         .bufferSize(numberOfConsumerThreads * 100)
         .build()
         .run();
   }

}// END OF INTERFACE Threads
