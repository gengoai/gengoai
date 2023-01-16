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

import com.gengoai.string.CharMatcher;
import com.gengoai.string.Strings;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class BrokerTest {

   @Test
   public void runTest() throws Exception {
      StringConsumer consumer = new StringConsumer();
      Broker<String> pc = Broker.<String>builder()
                             .bufferSize(100)
                             .addConsumer(consumer)
                             .addProducer(new RandomStringProducer())
                             .build();
      pc.run();
      assertEquals(100, consumer.ai.get());
   }

   public static class RandomStringProducer extends Broker.Producer<String> {

      @Override
      public void produce() {
         start();
         for (int i = 0; i < 100; i++) {
            String s = Strings.randomString(3, CharMatcher.LetterOrDigit);
            yieldObject(s);
         }
         stop();
      }

   }

   public static class StringConsumer implements java.util.function.Consumer<String> {
      public final AtomicInteger ai = new AtomicInteger();

      @Override
      public void accept(String input) {
         if (Strings.isNotNullOrBlank(input)) {
            ai.incrementAndGet();
         }
      }
   }

}//END OF ProducerConsumerTest
