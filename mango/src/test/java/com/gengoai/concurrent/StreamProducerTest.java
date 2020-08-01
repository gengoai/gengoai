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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class StreamProducerTest {

   private static class SummingConsumer implements Consumer<Integer> {
      public final AtomicInteger sum = new AtomicInteger();

      @Override
      public void accept(Integer integer) {
         sum.accumulateAndGet(integer, (i1, i2) -> i1 + i2);
      }
   }

   @Test
   public void produce() throws Exception {
      SummingConsumer consumer = new SummingConsumer();
      Broker<Integer> pc = Broker.<Integer>builder()
                              .bufferSize(100)
                              .addConsumer(consumer)
                              .addProducer(new StreamProducer<>(Stream.of(1, 2, 3, 4, 5)))
                              .build();
      pc.run();
      assertEquals(15, consumer.sum.get(), 0);
   }


}