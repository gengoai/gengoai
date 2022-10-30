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

package com.gengoai.stream.local;

import com.gengoai.Validation;
import com.gengoai.collection.Lists;
import com.gengoai.conversion.Cast;
import com.gengoai.function.SerializableRunnable;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import com.gengoai.stream.Streams;
import lombok.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The type Local in memory m stream.
 *
 * @param <T> the type parameter
 */
public class LocalInMemoryMStream<T> extends AbstractLocalMStream<T> {
   private static final long serialVersionUID = 1L;
   /**
    * The Collection.
    */
   final List<T> collection;
   private SerializableRunnable onClose;
   private boolean parallel = false;

   /**
    * Instantiates a new Local in memory m stream.
    *
    * @param collection the collection
    */
   public LocalInMemoryMStream(@NonNull Collection<T> collection) {
      if(collection instanceof List) {
         this.collection = Cast.as(collection);
      }
      else {
         this.collection = new ArrayList<>(collection);
      }
   }

   @Override
   public void close() throws IOException {
      if(onClose != null) {
         onClose.run();
      }
      try {
         this.collection.clear();
      } catch(UnsupportedOperationException uoe) {
         //noopt
      }
   }

   @Override
   public MStream<T> parallel() {
      this.parallel = true;
      return this;
   }

   @Override
   public MStream<Stream<T>> partition(long partitionSize) {
      int numPartitions = (int) (collection.size() / partitionSize);
      return new LocalReusableMStream<>(
            () -> IntStream.range(0, numPartitions)
                           .mapToObj(i -> new LocalInMemoryMStream<>(collection.subList(i,
                                                                                        (int) Math.min(i + partitionSize,
                                                                                                       collection.size()))))
                           .map(Cast::as)
      );
   }

   @Override
   public MStream<T> onClose(SerializableRunnable closeHandler) {
      if(onClose == null) {
         this.onClose = closeHandler;
      }
      else if(closeHandler != null) {
         this.onClose = SerializableRunnable.chain(onClose, closeHandler);
      }
      return this;
   }

   @Override
   public MStream<T> cache() {
      return this;
   }

   @Override
   public Stream<T> javaStream() {
      if(parallel) {
         return Streams.reusableParallelStream(collection);
      }
      return Streams.reusableStream(collection);
   }

   @Override
   public MStream<T> sample(boolean withReplacement, int number) {
      Validation.checkArgument(number >= 0, "Sample size must be non-negative.");
      if(number == 0) {
         return StreamingContext.local().empty();
      }
      if(withReplacement) {
         return new LocalInMemoryMStream<>(Lists.sampleWithReplacement(collection, number));
      }
      List<T> sample = new ArrayList<>();
      new Random().ints(0,collection.size())
                  .distinct()
                  .limit(Math.min(number,collection.size()))
                  .forEach(index -> sample.add(collection.get(index)));
      return new LocalInMemoryMStream<>(sample);
   }

   @Override
   public MStream<T> skip(long n) {
      if(n >= collection.size()) {
         return StreamingContext.local().empty();
      }
      return new LocalInMemoryMStream<>(collection.subList((int) n, collection.size()));
   }

}//END OF LocalInMemoryMStream
