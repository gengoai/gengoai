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

import java.util.stream.Stream;

/**
 * <p>A producer implementation that produces items from a stream</p>
 *
 * @param <V> the type of item being produced.
 * @author David B. Bracewell
 */
public class StreamProducer<V> extends Broker.Producer<V> {
   private final Stream<? extends V> stream;

   /**
    * Instantiates a new Stream producer.
    *
    * @param stream the stream
    */
   public StreamProducer(Stream<? extends V> stream) {
      this.stream = stream;
   }

   @Override
   public void produce() {
      start();
      stream.forEach(this::yieldObject);
      stop();
   }

}//END OF StreamProducer
