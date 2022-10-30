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

import com.gengoai.LogUtils;
import com.gengoai.Validation;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gengoai.LogUtils.logWarning;

/**
 * <p>An implementation of the Producer Consumer problem in which one or more producers are generating data for one or
 * more consumers to process.</p>
 *
 * @author David B. Bracewell
 */
@Log
public class Broker<V> implements Serializable {
   private static final long serialVersionUID = 1L;
   final ArrayBlockingQueue<V> queue;
   final List<Producer<V>> producers;
   final List<java.util.function.Consumer<? super V>> consumers;
   final AtomicInteger runningProducers = new AtomicInteger();

   private Broker(ArrayBlockingQueue<V> queue,
                  List<Producer<V>> producers,
                  List<java.util.function.Consumer<? super V>> consumers) {
      this.queue = queue;
      this.producers = producers;
      this.consumers = consumers;
   }

   /**
    * Creates a Builder to construct Broker instances.
    *
    * @return A builder instance
    */
   public static <V> Builder<V> builder() {
      return new Builder<>();
   }

   /**
    * Starts execution of the broker.
    *
    * @return Returns true if the broker completed without interruption, false otherwise
    */
   public boolean run() {
      ExecutorService executors = Executors.newFixedThreadPool(producers.size() + consumers.size());
      runningProducers.set(producers.size());

      //create the producers
      for(Producer<V> producer : producers) {
         producer.setOwner(this);
         executors.submit(new ProducerThread(producer));
      }

      //create the consumers
      for(java.util.function.Consumer<? super V> consumer : consumers) {
         executors.submit(new ConsumerThread(consumer));
      }

      //give it some more time to process
      while(runningProducers.get() > 0 || !queue.isEmpty()) {
         Threads.sleep(10);
      }

      executors.shutdown();
      try {
         executors.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
      } catch(InterruptedException e) {
         logWarning(log, e);
         return false;
      }
      return true;
   }

   /**
    * <p>A producer generates data to be consumed. Implementations of Producer should use the {@link #start()} to begin
    * the production process, {@link #stop()} to signal production has finished, and {@link #yield(Object)} to offer an
    * item up for consumption.</p>
    */
   @Log
   public abstract static class Producer<V> {

      Broker<V> owner;
      boolean isStopped = false;

      /**
       * @return True if the producer is still running.
       */
      public boolean isRunning() {
         return !isStopped;
      }

      /**
       * Logic for producing items to be consumed.
       */
      public abstract void produce();

      private void setOwner(Broker<V> owner) {
         this.owner = owner;
      }

      /**
       * Signals the production has started.
       */
      protected void start() {
         isStopped = false;
      }

      /**
       * Signals that the producer is finished and its thread can be released.
       */
      protected void stop() {
         isStopped = true;
         owner.runningProducers.decrementAndGet();
      }

      /**
       * offers an object to be consumed, blocking if the Broker's queue is full.
       *
       * @param object the object
       */
      protected final void yield(V object) {
         try {
            owner.queue.put(object);
         } catch(InterruptedException e) {
            logWarning(log, e);
         }
      }

   }//END OF ProducerConsumer$Producer

   /**
    * A Builder interface for constructing a Broker.
    */
   public static class Builder<V> {

      private ArrayBlockingQueue<V> queue;
      private List<Producer<V>> producers = new ArrayList<>();
      private List<java.util.function.Consumer<? super V>> consumers = new ArrayList<>();

      /**
       * Adds a  consumer.
       *
       * @param consumer the consumer
       * @return the builder
       */
      public Builder<V> addConsumer(java.util.function.Consumer<? super V> consumer) {
         return addConsumer(consumer, 1);
      }

      /**
       * Adds a  consumer and will run it on a number of threads.
       *
       * @param consumer the consumer
       * @param number   the number of threads to run the consumer on.
       * @return the builder
       */
      public Builder<V> addConsumer(java.util.function.Consumer<? super V> consumer, int number) {
         for(int i = 0; i < number; i++) {
            this.consumers.add(consumer);
         }
         return this;
      }

      /**
       * Add a collection of consumers.
       *
       * @param consumers the consumers
       * @return the builder
       */
      public Builder<V> addConsumers(Collection<? extends java.util.function.Consumer<? super V>> consumers) {
         this.consumers.addAll(consumers);
         return this;
      }

      /**
       * Adds producer and sets it to run on a number of threads. Note that the producer must be thread safe.
       *
       * @param producer the producer
       * @param number   the number of threads to run the producer on.
       * @return the builder
       */
      public Builder<V> addProducer(Producer<V> producer, int number) {
         for(int i = 0; i < number; i++) {
            this.producers.add(producer);
         }
         return this;
      }

      /**
       * Adds a producer
       *
       * @param producer the producer
       * @return the builder
       */
      public Builder<V> addProducer(Producer<V> producer) {
         return addProducer(producer, 1);
      }

      /**
       * Adds a collection of producers.
       *
       * @param producers the producers
       * @return the builder
       */
      public Builder<V> addProducers(Collection<? extends Producer<V>> producers) {
         this.producers.addAll(producers);
         return this;
      }

      /**
       * The size of the buffer.
       *
       * @param size the size
       * @return the builder
       */
      public Builder<V> bufferSize(int size) {
         Validation.checkArgument(size > 0);
         queue = new ArrayBlockingQueue<>(size);
         return this;
      }

      /**
       * Builds A Broker. If no queue size was given than it will default to <code>2 * (number of producers + number of
       * consumers)</code>
       *
       * @return the producer consumer
       */
      public Broker<V> build() {
         Validation.checkArgument(producers.size() > 0);
         Validation.checkArgument(consumers.size() > 0);
         if(queue == null) {
            queue = new ArrayBlockingQueue<>(10 * (producers.size() + consumers.size()));
         }
         return new Broker<>(queue, producers, consumers);
      }

   }//END OF ProducerConsumer$Builder

   private class ProducerThread implements Runnable {

      final Producer<V> producer;

      private ProducerThread(Producer<V> producer) {
         this.producer = producer;
      }

      @Override
      public void run() {
         while(!Thread.currentThread().isInterrupted() && producer.isRunning()) {
            try {
               producer.produce();
            } catch(Exception e) {
               logWarning(LogUtils.getLogger(getClass()), e);
            }
         }
      }

   }//END OF Broker$ProducerThread

   private class ConsumerThread implements Runnable {

      final java.util.function.Consumer<? super V> consumerAction;

      private ConsumerThread(java.util.function.Consumer<? super V> consumerAction) {
         this.consumerAction = consumerAction;
      }

      @Override
      public void run() {
         while(!Thread.currentThread().isInterrupted()) {
            try {
               V v = queue.poll(100, TimeUnit.NANOSECONDS);
               if(v != null) {
                  consumerAction.accept(v);
               }
               if(runningProducers.get() <= 0 && queue.isEmpty()) {
                  break;
               }
            } catch(InterruptedException e) {
               Thread.currentThread().interrupt();
            } catch(Exception e) {
               logWarning(LogUtils.getLogger(getClass()), e);
            }
         }
      }

   }//END OF Broker$ConsumerThread


}//END OF Broker
