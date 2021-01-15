/*
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

import com.gengoai.Validation;
import com.gengoai.function.Unchecked;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The type Broker iterator.
 *
 * @param <T> the type parameter
 * @param <O> the type parameter
 */
public class BrokerIterator<T, O> implements Iterator<O> {
   private final ArrayBlockingQueue<O> queue;
   private final Broker<T> broker;
   private final Thread thread;
   private final AtomicBoolean isRunning = new AtomicBoolean(true);


   /**
    * Instantiates a new Broker iterator.
    *
    * @param producer  the producer
    * @param converter the converter
    * @param nThreads  the n threads
    */
   public BrokerIterator(Broker.Producer<T> producer,
                         Function<? super T, Stream<? extends O>> converter,
                         int bufferSize,
                         int nThreads) {
      Validation.checkArgument(nThreads > 0);
      this.queue = new ArrayBlockingQueue<>(bufferSize);
      this.broker = Broker.<T>builder()
            .addProducer(producer)
            .addConsumer(Unchecked.consumer(o -> converter.apply(o).forEach(Unchecked.consumer(queue::put))), nThreads)
            .build();
      thread = new Thread(() -> {
         broker.run();
         isRunning.set(false);
      });
      thread.start();
   }

   @Override
   public boolean hasNext() {
      while(!Thread.currentThread().isInterrupted() && isRunning.get() && queue.isEmpty()) {
         Threads.sleep(100);
      }
      if(!isRunning.get()) {
         thread.interrupt();
      }
      return isRunning.get() || queue.size() > 0;
   }

   @Override
   public O next() {
      return queue.remove();
   }

}//END OF BrokerIterator
